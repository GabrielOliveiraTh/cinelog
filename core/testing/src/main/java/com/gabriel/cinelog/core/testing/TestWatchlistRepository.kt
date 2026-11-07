package com.gabriel.cinelog.core.testing

import com.gabriel.cinelog.core.data.WatchlistRepository
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.WatchlistItem
import com.gabriel.cinelog.core.model.WatchlistStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

// Movies must be registered first, as the real repository needs them cached in Room
class TestWatchlistRepository @Inject constructor() : WatchlistRepository {

    private data class Entry(val addedAt: Long, val watched: Boolean, val releaseNotified: Boolean)

    private val movies = mutableMapOf<Long, Movie>()
    private val entries = MutableStateFlow<Map<Long, Entry>>(emptyMap())
    private var clock = 0L

    var today: LocalDate = LocalDate(2026, 1, 1)

    fun registerMovies(vararg movie: Movie) {
        movie.forEach { movies[it.id] = it }
    }

    override fun observeWatchlist(): Flow<List<WatchlistItem>> = entries.map { current ->
        current.entries
            .sortedByDescending { it.value.addedAt }
            .mapNotNull { (movieId, entry) ->
                movies[movieId]?.let { WatchlistItem(it, entry.watched, entry.addedAt) }
            }
    }

    override fun observeStatus(movieId: Long): Flow<WatchlistStatus> = entries
        .map { current ->
            when (current[movieId]?.watched) {
                null -> WatchlistStatus.NotInWatchlist
                false -> WatchlistStatus.ToWatch
                true -> WatchlistStatus.Watched
            }
        }
        .distinctUntilChanged()

    override suspend fun add(movieId: Long) {
        entries.update { it + (movieId to Entry(++clock, watched = false, releaseNotified = isReleased(movieId))) }
    }

    override suspend fun remove(movieId: Long) {
        entries.update { it - movieId }
    }

    override suspend fun setWatched(movieId: Long, watched: Boolean) {
        entries.update { current ->
            val entry = current[movieId] ?: return@update current
            current + (movieId to entry.copy(watched = watched))
        }
    }

    override suspend fun restore(item: WatchlistItem) {
        registerMovies(item.movie)
        entries.update {
            it + (item.movie.id to Entry(item.addedAtMillis, item.isWatched, isReleased(item.movie.id)))
        }
    }

    override suspend fun getMovieIdsToWatch(): List<Long> = entries.value.filterValues { !it.watched }.keys.toList()

    override suspend fun getNewlyReleasedMovies(): List<Movie> = entries.value
        .filter { (movieId, entry) -> !entry.watched && !entry.releaseNotified && isReleased(movieId) }
        .keys
        .mapNotNull(movies::get)

    override suspend fun markReleaseNotified(movieIds: List<Long>) {
        entries.update { current ->
            current.mapValues { (movieId, entry) ->
                if (movieId in movieIds) entry.copy(releaseNotified = true) else entry
            }
        }
    }

    private fun isReleased(movieId: Long): Boolean = movies[movieId]?.releaseDate?.let { it <= today } ?: false
}
