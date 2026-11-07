package com.gabriel.cinelog.core.data

import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.WatchlistItem
import com.gabriel.cinelog.core.model.WatchlistStatus
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    /** Most recently added first. */
    fun observeWatchlist(): Flow<List<WatchlistItem>>

    fun observeStatus(movieId: Long): Flow<WatchlistStatus>

    /** The movie must already be cached. */
    suspend fun add(movieId: Long)

    suspend fun remove(movieId: Long)

    suspend fun setWatched(movieId: Long, watched: Boolean)

    /** Undoes a removal, keeping the original position. */
    suspend fun restore(item: WatchlistItem)

    suspend fun getMovieIdsToWatch(): List<Long>

    /** Movies to watch whose release date arrived and were not announced yet. */
    suspend fun getNewlyReleasedMovies(): List<Movie>

    suspend fun markReleaseNotified(movieIds: List<Long>)
}
