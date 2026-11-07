package com.gabriel.cinelog.sync.work

import androidx.paging.PagingData
import com.gabriel.cinelog.core.data.MovieRepository
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.MovieDetails
import com.gabriel.cinelog.core.testing.TestWatchlistRepository
import com.gabriel.cinelog.sync.work.notifications.Notifier
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchlistSyncerTest {

    private val watchlist = TestWatchlistRepository().apply { today = TODAY }
    private val movies = FakeMovieRepository(watchlist)
    private val notifier = FakeNotifier()
    private val syncer = WatchlistSyncer(watchlist, movies, notifier)

    @Test
    fun `a movie released since it was added is announced once`() = runTest {
        val upcoming = movie(1, "Duna: Parte Três", releaseDate = TODAY.plusDays(10))
        watchlist.registerMovies(upcoming)
        watchlist.add(upcoming.id)

        syncer.sync()
        assertEquals(emptyList<String>(), notifier.notifiedTitles)

        // Ten days later the release date arrives
        watchlist.today = TODAY.plusDays(10)
        syncer.sync()
        syncer.sync()

        assertEquals(listOf("Duna: Parte Três"), notifier.notifiedTitles)
    }

    @Test
    fun `movies already out when added are never announced`() = runTest {
        val released = movie(1, "Oppenheimer", releaseDate = TODAY.plusDays(-30))
        watchlist.registerMovies(released)
        watchlist.add(released.id)

        syncer.sync()

        assertEquals(emptyList<String>(), notifier.notifiedTitles)
    }

    @Test
    fun `a release date moved earlier by the refresh is announced`() = runTest {
        val upcoming = movie(1, "Avatar 3", releaseDate = TODAY.plusDays(60))
        watchlist.registerMovies(upcoming)
        watchlist.add(upcoming.id)
        // TMDB now says it came out yesterday
        movies.updatedReleaseDates[upcoming.id] = TODAY.plusDays(-1)

        syncer.sync()

        assertEquals(listOf("Avatar 3"), notifier.notifiedTitles)
    }

    @Test
    fun `without notification permission the release stays pending`() = runTest {
        val upcoming = movie(1, "Coringa 3", releaseDate = TODAY.plusDays(1))
        watchlist.registerMovies(upcoming)
        watchlist.add(upcoming.id)
        watchlist.today = TODAY.plusDays(1)

        notifier.permissionGranted = false
        syncer.sync()
        notifier.permissionGranted = true
        syncer.sync()

        assertEquals(listOf("Coringa 3"), notifier.notifiedTitles)
    }

    @Test
    fun `asks for a retry only when every refresh failed`() = runTest {
        watchlist.registerMovies(movie(1, "A"), movie(2, "B"))
        watchlist.add(1)
        watchlist.add(2)

        movies.failingIds += 1L
        assertTrue(syncer.sync())

        movies.failingIds += 2L
        assertFalse(syncer.sync())
    }

    @Test
    fun `empty watchlist succeeds without network`() = runTest {
        assertTrue(syncer.sync())
        assertEquals(emptyList<Long>(), movies.refreshedIds)
    }

    private companion object {
        val TODAY = LocalDate(2026, 11, 6)

        fun LocalDate.plusDays(days: Int): LocalDate = LocalDate.fromEpochDays(toEpochDays() + days)

        fun movie(id: Long, title: String, releaseDate: LocalDate? = null) =
            Movie(id, title, "", null, null, releaseDate, 7.0)
    }

    private class FakeNotifier : Notifier {
        var permissionGranted = true
        val notifiedTitles = mutableListOf<String>()

        override fun notifyReleased(movies: List<Movie>): Boolean {
            if (!permissionGranted) return false
            notifiedTitles += movies.map { it.title }
            return true
        }
    }

    /** Refreshing a movie applies the release date "TMDB" returns, like the real cache update. */
    private class FakeMovieRepository(private val watchlist: TestWatchlistRepository) : MovieRepository {
        val failingIds = mutableSetOf<Long>()
        val updatedReleaseDates = mutableMapOf<Long, LocalDate>()
        val refreshedIds = mutableListOf<Long>()

        override suspend fun refreshMovieDetails(movieId: Long) {
            if (movieId in failingIds) throw IOException("offline")
            refreshedIds += movieId
            updatedReleaseDates[movieId]?.let { date ->
                watchlist.registerMovies(movie(movieId, "Avatar 3", releaseDate = date))
            }
        }

        override fun popularMovies(): Flow<PagingData<Movie>> = emptyFlow()

        override fun searchMovies(query: String): Flow<PagingData<Movie>> = emptyFlow()

        override fun observeMovieDetails(movieId: Long): Flow<MovieDetails?> = emptyFlow()
    }
}
