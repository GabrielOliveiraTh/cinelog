package com.gabriel.cinelog.core.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.gabriel.cinelog.core.data.model.asCastEntities
import com.gabriel.cinelog.core.data.model.asDetailsEntity
import com.gabriel.cinelog.core.data.model.asMovieEntity
import com.gabriel.cinelog.core.data.paging.PopularMoviesRemoteMediator
import com.gabriel.cinelog.core.data.paging.SearchMoviesPagingSource
import com.gabriel.cinelog.core.data.paging.TMDB_PAGE_SIZE
import com.gabriel.cinelog.core.database.dao.MovieDao
import com.gabriel.cinelog.core.database.dao.MovieDetailsDao
import com.gabriel.cinelog.core.database.dao.PopularMovieDao
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.database.model.asExternalModel
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.MovieDetails
import com.gabriel.cinelog.core.network.CinelogNetworkDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class OfflineFirstMovieRepository @Inject constructor(
    private val popularMovieDao: PopularMovieDao,
    private val movieDetailsDao: MovieDetailsDao,
    private val movieDao: MovieDao,
    private val network: CinelogNetworkDataSource,
) : MovieRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun popularMovies(): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = TMDB_PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = PopularMoviesRemoteMediator(popularMovieDao, network),
        pagingSourceFactory = popularMovieDao::pagingSource,
    ).flow.map { pagingData -> pagingData.map(MovieEntity::asExternalModel) }

    override fun searchMovies(query: String): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = TMDB_PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { SearchMoviesPagingSource(query, network, movieDao) },
    ).flow

    override fun observeMovieDetails(movieId: Long): Flow<MovieDetails?> = movieDetailsDao.observeMovieDetails(movieId)
        .map { it?.asExternalModel() }
        // Room re-emits on any write to the joined tables; skip emissions that change nothing
        .distinctUntilChanged()

    override suspend fun refreshMovieDetails(movieId: Long) {
        val networkDetails = network.getMovieDetails(movieId)
        movieDetailsDao.saveDetails(
            movie = networkDetails.asMovieEntity(),
            details = networkDetails.asDetailsEntity(updatedAt = System.currentTimeMillis()),
            cast = networkDetails.asCastEntities(),
        )
    }
}
