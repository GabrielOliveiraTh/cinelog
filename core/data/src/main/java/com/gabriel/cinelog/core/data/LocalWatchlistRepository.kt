package com.gabriel.cinelog.core.data

import com.gabriel.cinelog.core.database.dao.WatchlistDao
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.database.model.WatchlistEntity
import com.gabriel.cinelog.core.database.model.WatchlistMovie
import com.gabriel.cinelog.core.database.model.asExternalModel
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.WatchlistItem
import com.gabriel.cinelog.core.model.WatchlistStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class LocalWatchlistRepository @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val dateProvider: DateProvider,
) : WatchlistRepository {

    override fun observeWatchlist(): Flow<List<WatchlistItem>> =
        watchlistDao.observeWatchlist().map { items -> items.map(WatchlistMovie::asExternalModel) }

    override fun observeStatus(movieId: Long): Flow<WatchlistStatus> = watchlistDao.observeEntry(movieId)
        .map { entry ->
            when {
                entry == null -> WatchlistStatus.NotInWatchlist
                entry.watchedAt == null -> WatchlistStatus.ToWatch
                else -> WatchlistStatus.Watched
            }
        }
        .distinctUntilChanged()

    override suspend fun add(movieId: Long) {
        watchlistDao.upsert(
            WatchlistEntity(
                movieId = movieId,
                addedAt = System.currentTimeMillis(),
                watchedAt = null,
                releaseNotified = isAlreadyReleased(movieId),
            ),
        )
    }

    override suspend fun remove(movieId: Long) {
        watchlistDao.delete(movieId)
    }

    override suspend fun setWatched(movieId: Long, watched: Boolean) {
        watchlistDao.setWatchedAt(movieId, watchedAt = if (watched) System.currentTimeMillis() else null)
    }

    override suspend fun restore(item: WatchlistItem) {
        watchlistDao.upsert(
            WatchlistEntity(
                movieId = item.movie.id,
                addedAt = item.addedAtMillis,
                // The exact watch date is not kept in the model; "watched" is what matters
                watchedAt = if (item.isWatched) System.currentTimeMillis() else null,
                releaseNotified = isAlreadyReleased(item.movie.id),
            ),
        )
    }

    override suspend fun getMovieIdsToWatch(): List<Long> = watchlistDao.getToWatchIds()

    override suspend fun getNewlyReleasedMovies(): List<Movie> =
        watchlistDao.getReleasedNotNotified(today = dateProvider.today().toString())
            .map(MovieEntity::asExternalModel)

    override suspend fun markReleaseNotified(movieIds: List<Long>) {
        watchlistDao.markReleaseNotified(movieIds)
    }

    // Unknown dates stay pending: the sync may fill them in later
    private suspend fun isAlreadyReleased(movieId: Long): Boolean {
        val releaseDate = watchlistDao.getReleaseDate(movieId)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return false
        return releaseDate <= dateProvider.today()
    }
}
