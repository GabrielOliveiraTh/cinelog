package com.gabriel.cinelog.core.data.paging

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.gabriel.cinelog.core.database.dao.MovieDao
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.network.CinelogNetworkDataSource
import com.gabriel.cinelog.core.network.model.NetworkMovie
import com.gabriel.cinelog.core.network.model.NetworkMovieDetails
import com.gabriel.cinelog.core.network.model.NetworkPagedResponse
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchMoviesPagingSourceTest {

    private val network = FakeSearchNetwork()
    private val movieDao = FakeMovieDao()
    private val pagingSource = SearchMoviesPagingSource("batman", network, movieDao)

    @Test
    fun `first page returns results, saves them and points to the next page`() = runTest {
        network.pages[1] = listOf(1L, 2L)

        val page = pagingSource.load(refresh()) as LoadResult.Page

        assertEquals(listOf(1L, 2L), page.data.map(Movie::id))
        assertEquals(2, page.nextKey)
        assertNull(page.prevKey)
        assertEquals(setOf(1L, 2L), movieDao.savedIds)
        assertEquals(listOf("batman" to 1), network.requests)
    }

    @Test
    fun `movies repeated on a later page are dropped`() = runTest {
        network.pages[1] = listOf(1L, 2L)
        network.pages[2] = listOf(2L, 3L)

        pagingSource.load(refresh())
        val second = pagingSource.load(append(key = 2)) as LoadResult.Page

        assertEquals(listOf(3L), second.data.map(Movie::id))
    }

    @Test
    fun `last page has no next key`() = runTest {
        network.totalPages = 1
        network.pages[1] = listOf(1L)

        val page = pagingSource.load(refresh()) as LoadResult.Page

        assertNull(page.nextKey)
    }

    @Test
    fun `network failure becomes a load error`() = runTest {
        network.failure = IOException("offline")

        val result = pagingSource.load(refresh())

        assertTrue(result is LoadResult.Error)
    }

    private fun refresh() = LoadParams.Refresh<Int>(key = null, loadSize = TMDB_PAGE_SIZE, placeholdersEnabled = false)

    private fun append(key: Int) = LoadParams.Append(key = key, loadSize = TMDB_PAGE_SIZE, placeholdersEnabled = false)

    private class FakeSearchNetwork : CinelogNetworkDataSource {
        val pages = mutableMapOf<Int, List<Long>>()
        var totalPages = 5
        var failure: Exception? = null
        val requests = mutableListOf<Pair<String, Int>>()

        override suspend fun searchMovies(query: String, page: Int): NetworkPagedResponse<NetworkMovie> {
            failure?.let { throw it }
            requests += query to page
            val ids = pages[page].orEmpty()
            return NetworkPagedResponse(
                page = page,
                results = ids.map { NetworkMovie(id = it, title = "Movie $it") },
                totalPages = totalPages,
                totalResults = totalPages * 20,
            )
        }

        override suspend fun getPopularMovies(page: Int): NetworkPagedResponse<NetworkMovie> =
            throw UnsupportedOperationException()

        override suspend fun getMovieDetails(movieId: Long): NetworkMovieDetails = throw UnsupportedOperationException()
    }

    private class FakeMovieDao : MovieDao {
        val savedIds = mutableSetOf<Long>()

        override suspend fun upsertMovies(movies: List<MovieEntity>) {
            savedIds += movies.map(MovieEntity::id)
        }
    }
}
