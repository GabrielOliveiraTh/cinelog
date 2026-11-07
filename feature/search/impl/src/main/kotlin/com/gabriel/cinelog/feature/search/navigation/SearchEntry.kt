package com.gabriel.cinelog.feature.search.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gabriel.cinelog.feature.search.SearchRoute

fun EntryProviderScope<NavKey>.searchEntry(onMovieClick: (movieId: Long) -> Unit, onBack: () -> Unit) {
    entry<SearchNavKey> {
        SearchRoute(onMovieClick = onMovieClick, onBack = onBack)
    }
}
