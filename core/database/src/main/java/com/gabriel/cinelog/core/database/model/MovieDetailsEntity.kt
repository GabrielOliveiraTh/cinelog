package com.gabriel.cinelog.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "movie_details",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["id"],
            childColumns = ["movie_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MovieDetailsEntity(
    @PrimaryKey @ColumnInfo(name = "movie_id") val movieId: Long,
    val tagline: String?,
    @ColumnInfo(name = "runtime_minutes") val runtimeMinutes: Int?,
    val genres: List<String>,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long,
)
