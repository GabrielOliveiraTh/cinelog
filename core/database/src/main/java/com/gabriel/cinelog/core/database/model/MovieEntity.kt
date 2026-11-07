package com.gabriel.cinelog.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gabriel.cinelog.core.model.Movie
import kotlinx.datetime.LocalDate

// Shared by every list; lists only reference it by id
@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val overview: String,
    @ColumnInfo(name = "poster_path") val posterPath: String?,
    @ColumnInfo(name = "backdrop_path") val backdropPath: String?,
    // yyyy-MM-dd, as sent by TMDB
    @ColumnInfo(name = "release_date") val releaseDate: String?,
    @ColumnInfo(name = "vote_average") val voteAverage: Double,
)

fun MovieEntity.asExternalModel() = Movie(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    voteAverage = voteAverage,
)
