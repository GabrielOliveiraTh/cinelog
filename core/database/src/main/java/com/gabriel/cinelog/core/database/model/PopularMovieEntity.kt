package com.gabriel.cinelog.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Movie id as the key: TMDB repeats movies across pages, and duplicate keys crash a Lazy list
@Entity(
    tableName = "popular_movies",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["id"],
            childColumns = ["movie_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("position")],
)
data class PopularMovieEntity(@PrimaryKey @ColumnInfo(name = "movie_id") val movieId: Long, val position: Int)
