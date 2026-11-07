package com.gabriel.cinelog.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

// Keyed by billing order: the same actor can play two characters in one movie
@Entity(
    tableName = "movie_cast",
    primaryKeys = ["movie_id", "billing_order"],
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["id"],
            childColumns = ["movie_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CastMemberEntity(
    @ColumnInfo(name = "movie_id") val movieId: Long,
    @ColumnInfo(name = "billing_order") val billingOrder: Int,
    @ColumnInfo(name = "person_id") val personId: Long,
    val name: String,
    val character: String,
    @ColumnInfo(name = "profile_path") val profilePath: String?,
)
