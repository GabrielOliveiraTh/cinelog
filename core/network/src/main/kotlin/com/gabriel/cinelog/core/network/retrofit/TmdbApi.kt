package com.gabriel.cinelog.core.network.retrofit

import com.gabriel.cinelog.core.network.model.NetworkMovie
import com.gabriel.cinelog.core.network.model.NetworkMovieDetails
import com.gabriel.cinelog.core.network.model.NetworkPagedResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface TmdbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int): NetworkPagedResponse<NetworkMovie>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
    ): NetworkPagedResponse<NetworkMovie>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Long,
        @Query("append_to_response") appendToResponse: String = "credits",
    ): NetworkMovieDetails
}
