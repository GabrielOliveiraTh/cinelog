package com.gabriel.cinelog.feature.discover.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gabriel.cinelog.feature.discover.DiscoverRoute

fun EntryProviderScope<NavKey>.discoverEntry(onMovieClick: (movieId: Long) -> Unit, onSearchClick: () -> Unit) {
    entry<DiscoverNavKey> {
        DiscoverRoute(onMovieClick = onMovieClick, onSearchClick = onSearchClick)
    }
}
