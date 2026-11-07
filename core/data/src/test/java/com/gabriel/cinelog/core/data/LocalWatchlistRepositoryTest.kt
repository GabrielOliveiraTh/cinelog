package com.gabriel.cinelog.core.data

import com.gabriel.cinelog.core.database.dao.WatchlistDao
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.database.model.WatchlistEntity
import com.gabriel.cinelog.core.database.model.WatchlistMovie
import com.gabriel.cinelog.core.database.model.asExternalModel
import com.gabriel.cinelog.core.model.WatchlistStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LocalWatchlistRepositoryTest {

    private val dao = FakeWatchlistDao()
    private val repository = LocalWatchlistRepository(dao, dateProvider = { TODAY })

    @Test
    fun `status follows the watchlist entry`() = runTest {
        assertEquals(WatchlistStatus.NotInWatchlist, repository.observeStatus(MOVIE_ID).first())

        repository.add(MOVIE_ID)
        assertEquals(WatchlistStatus.ToWatch, repository.observeStatus(MOVIE_ID).first())

        repository.setWatched(MOVIE_ID, watched = true)
        assertEquals(WatchlistStatus.Watched, repository.observeStatus(MOVIE_ID).first())
        assertNotNull(dao.entries.value[MOVIE_ID]?.watchedAt)

        repository.setWatched(MOVIE_ID, watched = false)
        assertNull(dao.entries.value[MOVIE_ID]?.watchedAt)
    }

    @Test
    fun `restore keeps the original added date`() = runTest {
        val item = WatchlistMovie(movie = movieEntity, addedAt = 42L, watchedAt = 100L).asExternalModel()

        repository.restore(item)

        val restored = dao.entries.value.getValue(MOVIE_ID)
        assertEquals(42L, restored.addedAt)
        assertNotNull(restored.watchedAt)
    }

    @Test
    fun `adding a movie already released skips its release notification`() = runTest {
        dao.releaseDates[MOVIE_ID] = "2026-10-01"
        dao.releaseDates[UPCOMING_ID] = "2026-12-25"

        repository.add(MOVIE_ID)
        repository.add(UPCOMING_ID)
        repository.add(UNKNOWN_DATE_ID)

        assertEquals(true, dao.entries.value.getValue(MOVIE_ID).releaseNotified)
        assertEquals(false, dao.entries.value.getValue(UPCOMING_ID).releaseNotified)
        // Unknown dates stay pending: a later sync may find out the date
        assertEquals(false, dao.entries.value.getValue(UNKNOWN_DATE_ID).releaseNotified)
    }

    private companion object {
        const val MOVIE_ID = 550L
        const val UPCOMING_ID = 1L
        const val UNKNOWN_DATE_ID = 2L
        val TODAY = LocalDate(2026, 11, 6)

        val movieEntity = MovieEntity(
            id = MOVIE_ID,
            title = "Clube da Luta",
            overview = "",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            voteAverage = 8.4,
        )
    }

    private class FakeWatchlistDao : WatchlistDao {
        val entries = MutableStateFlow<Map<Long, WatchlistEntity>>(emptyMap())

        override fun observeWatchlist(): Flow<List<WatchlistMovie>> = throw UnsupportedOperationException()

        override fun observeEntry(movieId: Long): Flow<WatchlistEntity?> = entries.map { it[movieId] }

        override suspend fun upsert(entry: WatchlistEntity) {
            entries.value += entry.movieId to entry
        }

        override suspend fun delete(movieId: Long) {
            entries.value -= movieId
        }

        override suspend fun setWatchedAt(movieId: Long, watchedAt: Long?) {
            val entry = entries.value[movieId] ?: return
            entries.value += movieId to entry.copy(watchedAt = watchedAt)
        }

        val releaseDates = mutableMapOf<Long, String>()

        override suspend fun getReleaseDate(movieId: Long): String? = releaseDates[movieId]

        override suspend fun getToWatchIds(): List<Long> = throw UnsupportedOperationException()

        override suspend fun getReleasedNotNotified(today: String): List<MovieEntity> =
            throw UnsupportedOperationException()

        override suspend fun markReleaseNotified(movieIds: List<Long>) = throw UnsupportedOperationException()
    }
}
