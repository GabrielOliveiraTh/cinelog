package com.gabriel.cinelog.core.network

import com.gabriel.cinelog.core.network.model.NetworkMovie
import com.gabriel.cinelog.core.network.model.NetworkMovieDetails
import com.gabriel.cinelog.core.network.model.NetworkPagedResponse

interface CinelogNetworkDataSource {
    suspend fun getPopularMovies(page: Int): NetworkPagedResponse<NetworkMovie>

    suspend fun searchMovies(query: String, page: Int): NetworkPagedResponse<NetworkMovie>

    suspend fun getMovieDetails(movieId: Long): NetworkMovieDetails
}
