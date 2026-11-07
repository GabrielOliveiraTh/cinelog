package com.gabriel.cinelog.sync.work

import com.gabriel.cinelog.core.data.MovieRepository
import com.gabriel.cinelog.core.data.WatchlistRepository
import com.gabriel.cinelog.core.data.suspendRunCatching
import com.gabriel.cinelog.sync.work.notifications.Notifier
import javax.inject.Inject

// Kept out of the Worker so it can be unit tested with fakes
internal class WatchlistSyncer @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val movieRepository: MovieRepository,
    private val notifier: Notifier,
) {
    /** Returns false when every refresh failed (likely offline), so the work is retried. */
    suspend fun sync(): Boolean {
        val movieIds = watchlistRepository.getMovieIdsToWatch()

        // One movie failing should not stop the others
        val failures = movieIds.count { movieId ->
            suspendRunCatching { movieRepository.refreshMovieDetails(movieId) }.isFailure
        }

        // Runs even if the refresh failed: the cached dates may already say a movie is out
        val released = watchlistRepository.getNewlyReleasedMovies()
        if (released.isNotEmpty() && notifier.notifyReleased(released)) {
            watchlistRepository.markReleaseNotified(released.map { it.id })
        }

        return movieIds.isEmpty() || failures < movieIds.size
    }
}
