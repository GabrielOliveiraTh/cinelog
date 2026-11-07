package com.gabriel.cinelog.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.InitializeAction
import androidx.paging.RemoteMediator.MediatorResult
import com.gabriel.cinelog.core.database.dao.PopularMovieDao
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.database.model.PopularMovieEntity
import com.gabriel.cinelog.core.database.model.RemoteKeyEntity
import com.gabriel.cinelog.core.network.CinelogNetworkDataSource
import com.gabriel.cinelog.core.network.model.NetworkMovie
import com.gabriel.cinelog.core.network.model.NetworkMovieDetails
import com.gabriel.cinelog.core.network.model.NetworkPagedResponse
import java.io.IOException
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class PopularMoviesRemoteMediatorTest {

    private val dao = FakePopularMovieDao()
    private val network = FakeNetworkDataSource()
    private var currentTime = 10_000_000L
    private val mediator = PopularMoviesRemoteMediator(dao, network, now = { currentTime })

    private val emptyState = PagingState<Int, MovieEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = TMDB_PAGE_SIZE),
        leadingPlaceholderCount = 0,
    )

    @Test
    fun `refresh fetches the first page and stores the next page key`() = runTest {
        val result = mediator.load(LoadType.REFRESH, emptyState)

        assertTrue(result is MediatorResult.Success)
        assertEquals(false, (result as MediatorResult.Success).endOfPaginationReached)
        assertEquals(listOf(1), network.requestedPages)
        assertEquals(listOf(100L, 101L), dao.orderedMovieIds())
        assertEquals(2, dao.key?.nextPage)
    }

    @Test
    fun `append continues from the stored key and keeps the order`() = runTest {
        mediator.load(LoadType.REFRESH, emptyState)

        mediator.load(LoadType.APPEND, emptyState)

        assertEquals(listOf(1, 2), network.requestedPages)
        assertEquals(listOf(100L, 101L, 200L, 201L), dao.orderedMovieIds())
    }

    @Test
    fun `last page ends pagination`() = runTest {
        network.totalPages = 1

        val result = mediator.load(LoadType.REFRESH, emptyState) as MediatorResult.Success

        assertTrue(result.endOfPaginationReached)
        assertNull(dao.key?.nextPage)
    }

    @Test
    fun `stops at the last page TMDB actually serves`() = runTest {
        network.totalPages = 1001
        dao.savePage(emptyList(), firstPosition = 0, nextPage = TMDB_MAX_PAGE, replace = true, updatedAt = 0)

        val result = mediator.load(LoadType.APPEND, emptyState) as MediatorResult.Success

        assertEquals(listOf(TMDB_MAX_PAGE), network.requestedPages)
        assertTrue(result.endOfPaginationReached)
        assertNull(dao.key?.nextPage)
    }

    @Test
    fun `refresh replaces the previous order`() = runTest {
        mediator.load(LoadType.REFRESH, emptyState)
        mediator.load(LoadType.APPEND, emptyState)

        mediator.load(LoadType.REFRESH, emptyState)

        assertEquals(listOf(100L, 101L), dao.orderedMovieIds())
    }

    @Test
    fun `network failure returns an error and leaves the cache untouched`() = runTest {
        mediator.load(LoadType.REFRESH, emptyState)
        network.failure = IOException("offline")

        val result = mediator.load(LoadType.REFRESH, emptyState)

        assertTrue(result is MediatorResult.Error)
        assertEquals(listOf(100L, 101L), dao.orderedMovieIds())
    }

    @Test
    fun `fresh cache skips the initial refresh, stale cache does not`() = runTest {
        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())

        mediator.load(LoadType.REFRESH, emptyState)
        currentTime += 30.minutes.inWholeMilliseconds
        assertEquals(InitializeAction.SKIP_INITIAL_REFRESH, mediator.initialize())

        currentTime += 31.minutes.inWholeMilliseconds
        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }
}

/** In-memory DAO: exercises the real [PopularMovieDao.savePage] logic without a database. */
private class FakePopularMovieDao : PopularMovieDao() {
    private val movies = mutableMapOf<Long, MovieEntity>()
    private val positions = mutableMapOf<Long, Int>()
    var key: RemoteKeyEntity? = null
        private set

    fun orderedMovieIds(): List<Long> = positions.entries.sortedBy { it.value }.map { it.key }

    override fun pagingSource(): PagingSource<Int, MovieEntity> = throw UnsupportedOperationException()

    override suspend fun remoteKey(): RemoteKeyEntity? = key

    override suspend fun upsertMovies(movies: List<MovieEntity>) {
        movies.forEach { this.movies[it.id] = it }
    }

    override suspend fun insertPositions(positions: List<PopularMovieEntity>) {
        positions.forEach { this.positions.putIfAbsent(it.movieId, it.position) }
    }

    override suspend fun upsertRemoteKey(key: RemoteKeyEntity) {
        this.key = key
    }

    override suspend fun clearPositions() {
        positions.clear()
    }
}

/** Each page returns two movies with ids `page * 100 + index`. */
private class FakeNetworkDataSource : CinelogNetworkDataSource {
    var totalPages = 3
    var failure: Exception? = null
    val requestedPages = mutableListOf<Int>()

    override suspend fun getPopularMovies(page: Int): NetworkPagedResponse<NetworkMovie> {
        failure?.let { throw it }
        requestedPages += page
        return NetworkPagedResponse(
            page = page,
            results = List(2) { index -> NetworkMovie(id = page * 100L + index, title = "Movie $page-$index") },
            totalPages = totalPages,
            totalResults = totalPages * 2,
        )
    }

    override suspend fun searchMovies(query: String, page: Int): NetworkPagedResponse<NetworkMovie> =
        throw UnsupportedOperationException()

    override suspend fun getMovieDetails(movieId: Long): NetworkMovieDetails = throw UnsupportedOperationException()
}
