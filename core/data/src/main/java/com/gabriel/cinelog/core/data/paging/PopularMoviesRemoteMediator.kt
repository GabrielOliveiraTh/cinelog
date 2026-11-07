package com.gabriel.cinelog.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.gabriel.cinelog.core.data.model.asEntity
import com.gabriel.cinelog.core.data.suspendRunCatching
import com.gabriel.cinelog.core.database.dao.PopularMovieDao
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.network.CinelogNetworkDataSource
import com.gabriel.cinelog.core.network.model.NetworkMovie
import kotlin.time.Duration.Companion.hours

private val CACHE_TIMEOUT = 1.hours

@OptIn(ExperimentalPagingApi::class)
internal class PopularMoviesRemoteMediator(
    private val dao: PopularMovieDao,
    private val network: CinelogNetworkDataSource,
    private val now: () -> Long = System::currentTimeMillis,
) : RemoteMediator<Int, MovieEntity>() {

    override suspend fun initialize(): InitializeAction {
        val lastUpdated = dao.remoteKey()?.lastUpdated ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val isCacheFresh = now() - lastUpdated < CACHE_TIMEOUT.inWholeMilliseconds
        return if (isCacheFresh) InitializeAction.SKIP_INITIAL_REFRESH else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, MovieEntity>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1

            // The feed always starts at page 1, so there is nothing before it
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> dao.remoteKey()?.nextPage
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return suspendRunCatching {
            val response = network.getPopularMovies(page)
            val nextPage = response.nextPage()
            dao.savePage(
                movies = response.results.map(NetworkMovie::asEntity),
                firstPosition = (page - 1) * TMDB_PAGE_SIZE,
                nextPage = nextPage,
                replace = loadType == LoadType.REFRESH,
                updatedAt = now(),
            )
            nextPage
        }.fold(
            onSuccess = { nextPage -> MediatorResult.Success(endOfPaginationReached = nextPage == null) },
            onFailure = { error -> MediatorResult.Error(error) },
        )
    }
}
