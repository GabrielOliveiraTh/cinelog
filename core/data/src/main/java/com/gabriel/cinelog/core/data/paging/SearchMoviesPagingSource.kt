package com.gabriel.cinelog.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.gabriel.cinelog.core.data.model.asEntity
import com.gabriel.cinelog.core.data.suspendRunCatching
import com.gabriel.cinelog.core.database.dao.MovieDao
import com.gabriel.cinelog.core.database.model.asExternalModel
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.network.CinelogNetworkDataSource
import com.gabriel.cinelog.core.network.model.NetworkMovie

// Network only: results depend on the query. Pages still go into the movies table so details open filled.
internal class SearchMoviesPagingSource(
    private val query: String,
    private val network: CinelogNetworkDataSource,
    private val movieDao: MovieDao,
) : PagingSource<Int, Movie>() {

    // TMDB may repeat a movie on a later page; duplicate ids would crash the Lazy grid
    private val seenIds = mutableSetOf<Long>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return suspendRunCatching {
            val response = network.searchMovies(query, page)
            val entities = response.results.map(NetworkMovie::asEntity)
            movieDao.upsertMovies(entities)
            LoadResult.Page(
                data = entities.filter { seenIds.add(it.id) }.map { it.asExternalModel() },
                prevKey = null,
                nextKey = response.nextPage(),
            )
        }.getOrElse { error -> LoadResult.Error(error) }
    }

    // Pages only go forward from page 1, so a refresh restarts from the beginning
    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null
}
