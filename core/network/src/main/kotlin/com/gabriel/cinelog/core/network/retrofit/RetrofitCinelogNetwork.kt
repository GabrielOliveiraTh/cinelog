package com.gabriel.cinelog.core.network.retrofit

import com.gabriel.cinelog.core.network.CinelogNetworkDataSource
import com.gabriel.cinelog.core.network.model.NetworkMovie
import com.gabriel.cinelog.core.network.model.NetworkMovieDetails
import com.gabriel.cinelog.core.network.model.NetworkPagedResponse
import javax.inject.Inject

internal class RetrofitCinelogNetwork @Inject constructor(private val api: TmdbApi) : CinelogNetworkDataSource {

    override suspend fun getPopularMovies(page: Int): NetworkPagedResponse<NetworkMovie> = api.getPopularMovies(page)

    override suspend fun searchMovies(query: String, page: Int): NetworkPagedResponse<NetworkMovie> =
        api.searchMovies(query, page)

    override suspend fun getMovieDetails(movieId: Long): NetworkMovieDetails = api.getMovieDetails(movieId)
}
