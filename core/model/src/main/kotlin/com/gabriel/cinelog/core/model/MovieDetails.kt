package com.gabriel.cinelog.core.model

data class MovieDetails(
    val movie: Movie,
    val tagline: String?,
    val runtimeMinutes: Int?,
    val genres: List<String>,
    val cast: List<CastMember>,
    // False while only the data cached from a list is known
    val hasFullDetails: Boolean,
)

data class CastMember(val id: Long, val name: String, val character: String, val profilePath: String?)
