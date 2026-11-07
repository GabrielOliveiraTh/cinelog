package com.gabriel.cinelog.core.data.model

import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.network.model.NetworkMovie

internal fun NetworkMovie.asEntity() = MovieEntity(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    // TMDB sends "" when the date is unknown
    releaseDate = releaseDate?.takeIf { it.isNotBlank() },
    voteAverage = voteAverage,
)
