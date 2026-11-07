package com.gabriel.cinelog.core.data

import androidx.paging.PagingData
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.MovieDetails
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun popularMovies(): Flow<PagingData<Movie>>

    fun searchMovies(query: String): Flow<PagingData<Movie>>

    /** Emits the cached details right away (often only the list data), then each refresh. Null if unknown. */
    fun observeMovieDetails(movieId: Long): Flow<MovieDetails?>

    /** Throws if the request fails. */
    suspend fun refreshMovieDetails(movieId: Long)
}
