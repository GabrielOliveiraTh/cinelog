package com.gabriel.cinelog.feature.details.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gabriel.cinelog.feature.details.MovieDetailsRoute

fun EntryProviderScope<NavKey>.movieDetailsEntry(onBack: () -> Unit) {
    entry<MovieDetailsNavKey> { key ->
        MovieDetailsRoute(movieId = key.movieId, onBack = onBack)
    }
}
