package com.gabriel.cinelog.core.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.gabriel.cinelog.core.model.CastMember
import com.gabriel.cinelog.core.model.MovieDetails

data class PopulatedMovieDetails(
    @Embedded val movie: MovieEntity,
    @Relation(parentColumn = "id", entityColumn = "movie_id")
    val details: MovieDetailsEntity?,
    @Relation(parentColumn = "id", entityColumn = "movie_id")
    val cast: List<CastMemberEntity>,
)

fun PopulatedMovieDetails.asExternalModel() = MovieDetails(
    movie = movie.asExternalModel(),
    tagline = details?.tagline,
    runtimeMinutes = details?.runtimeMinutes,
    genres = details?.genres.orEmpty(),
    // @Relation does not guarantee order
    cast = cast.sortedBy { it.billingOrder }.map {
        CastMember(id = it.personId, name = it.name, character = it.character, profilePath = it.profilePath)
    },
    hasFullDetails = details != null,
)
