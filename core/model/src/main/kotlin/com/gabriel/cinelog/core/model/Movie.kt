package com.gabriel.cinelog.core.model

import kotlinx.datetime.LocalDate

data class Movie(
    val id: Long,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: LocalDate?,
    val voteAverage: Double,
)
