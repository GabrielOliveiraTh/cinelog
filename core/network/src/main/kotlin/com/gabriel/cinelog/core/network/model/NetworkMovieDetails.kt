package com.gabriel.cinelog.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkMovieDetails(
    val id: Long,
    val title: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    val tagline: String? = null,
    // TMDB sends 0 or null when unknown
    val runtime: Int? = null,
    val genres: List<NetworkGenre> = emptyList(),
    val credits: NetworkCredits = NetworkCredits(),
)

@Serializable
data class NetworkGenre(val id: Long, val name: String)

@Serializable
data class NetworkCredits(val cast: List<NetworkCastMember> = emptyList())

@Serializable
data class NetworkCastMember(
    val id: Long,
    val name: String,
    val character: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
    // 0 is the lead
    val order: Int = Int.MAX_VALUE,
)
