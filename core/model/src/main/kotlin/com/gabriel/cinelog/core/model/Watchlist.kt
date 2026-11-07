package com.gabriel.cinelog.core.model

enum class WatchlistStatus { NotInWatchlist, ToWatch, Watched }

data class WatchlistItem(
    val movie: Movie,
    val isWatched: Boolean,
    /** Kept so an undone removal puts the movie back in its original position. */
    val addedAtMillis: Long,
)
