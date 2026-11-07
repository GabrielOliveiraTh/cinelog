package com.gabriel.cinelog.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.gabriel.cinelog.core.model.WatchlistItem

// The only user data in the database. No CASCADE on purpose: clearing cached movies must fail
// rather than silently erase someone's list.
@Entity(
    tableName = "watchlist",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["id"],
            childColumns = ["movie_id"],
        ),
    ],
)
data class WatchlistEntity(
    @PrimaryKey @ColumnInfo(name = "movie_id") val movieId: Long,
    @ColumnInfo(name = "added_at") val addedAt: Long,
    @ColumnInfo(name = "watched_at") val watchedAt: Long?,
    // Also true when the movie was already out when added. The default lets the migration fill existing rows.
    @ColumnInfo(name = "release_notified", defaultValue = "0") val releaseNotified: Boolean = false,
)

data class WatchlistMovie(
    @Embedded val movie: MovieEntity,
    @ColumnInfo(name = "added_at") val addedAt: Long,
    @ColumnInfo(name = "watched_at") val watchedAt: Long?,
)

fun WatchlistMovie.asExternalModel() = WatchlistItem(
    movie = movie.asExternalModel(),
    isWatched = watchedAt != null,
    addedAtMillis = addedAt,
)
