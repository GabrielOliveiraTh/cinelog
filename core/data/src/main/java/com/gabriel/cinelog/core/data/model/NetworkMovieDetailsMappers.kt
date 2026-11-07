package com.gabriel.cinelog.core.data.model

import com.gabriel.cinelog.core.database.model.CastMemberEntity
import com.gabriel.cinelog.core.database.model.MovieDetailsEntity
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.network.model.NetworkMovieDetails

// TMDB returns over 100 names for big movies
internal const val MAX_CAST_MEMBERS = 15

internal fun NetworkMovieDetails.asMovieEntity() = MovieEntity(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate?.takeIf { it.isNotBlank() },
    voteAverage = voteAverage,
)

internal fun NetworkMovieDetails.asDetailsEntity(updatedAt: Long) = MovieDetailsEntity(
    movieId = id,
    tagline = tagline?.takeIf { it.isNotBlank() },
    runtimeMinutes = runtime?.takeIf { it > 0 },
    genres = genres.map { it.name },
    lastUpdated = updatedAt,
)

internal fun NetworkMovieDetails.asCastEntities(): List<CastMemberEntity> = credits.cast
    .sortedBy { it.order }
    .take(MAX_CAST_MEMBERS)
    .mapIndexed { index, member ->
        CastMemberEntity(
            movieId = id,
            billingOrder = index,
            personId = member.id,
            name = member.name,
            character = member.character,
            profilePath = member.profilePath,
        )
    }
