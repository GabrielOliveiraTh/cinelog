package com.gabriel.cinelog.feature.watchlist.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gabriel.cinelog.feature.watchlist.WatchlistRoute

fun EntryProviderScope<NavKey>.watchlistEntry(onMovieClick: (movieId: Long) -> Unit) {
    entry<WatchlistNavKey> {
        WatchlistRoute(onMovieClick = onMovieClick)
    }
}
