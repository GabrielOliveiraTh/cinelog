package com.gabriel.cinelog.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.database.model.WatchlistEntity
import com.gabriel.cinelog.core.database.model.WatchlistMovie
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query(
        """
        SELECT movies.*, watchlist.added_at, watchlist.watched_at FROM watchlist
        INNER JOIN movies ON movies.id = watchlist.movie_id
        ORDER BY watchlist.added_at DESC
        """,
    )
    fun observeWatchlist(): Flow<List<WatchlistMovie>>

    @Query("SELECT * FROM watchlist WHERE movie_id = :movieId")
    fun observeEntry(movieId: Long): Flow<WatchlistEntity?>

    @Upsert
    suspend fun upsert(entry: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movie_id = :movieId")
    suspend fun delete(movieId: Long)

    @Query("UPDATE watchlist SET watched_at = :watchedAt WHERE movie_id = :movieId")
    suspend fun setWatchedAt(movieId: Long, watchedAt: Long?)

    @Query("SELECT release_date FROM movies WHERE id = :movieId")
    suspend fun getReleaseDate(movieId: Long): String?

    @Query("SELECT movie_id FROM watchlist WHERE watched_at IS NULL")
    suspend fun getToWatchIds(): List<Long>

    // ISO dates (yyyy-MM-dd) compare correctly as plain strings
    @Query(
        """
        SELECT movies.* FROM watchlist
        INNER JOIN movies ON movies.id = watchlist.movie_id
        WHERE watchlist.watched_at IS NULL
          AND watchlist.release_notified = 0
          AND movies.release_date IS NOT NULL
          AND movies.release_date <= :today
        """,
    )
    suspend fun getReleasedNotNotified(today: String): List<MovieEntity>

    @Query("UPDATE watchlist SET release_notified = 1 WHERE movie_id IN (:movieIds)")
    suspend fun markReleaseNotified(movieIds: List<Long>)
}
