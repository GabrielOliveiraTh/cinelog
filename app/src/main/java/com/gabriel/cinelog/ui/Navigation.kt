/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gabriel.cinelog.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.gabriel.cinelog.R
import com.gabriel.cinelog.core.designsystem.R as DesignSystemR
import com.gabriel.cinelog.feature.details.navigation.MovieDetailsNavKey
import com.gabriel.cinelog.feature.details.navigation.movieDetailsEntry
import com.gabriel.cinelog.feature.discover.navigation.DiscoverNavKey
import com.gabriel.cinelog.feature.discover.navigation.discoverEntry
import com.gabriel.cinelog.feature.search.navigation.SearchNavKey
import com.gabriel.cinelog.feature.search.navigation.searchEntry
import com.gabriel.cinelog.feature.watchlist.navigation.WatchlistNavKey
import com.gabriel.cinelog.feature.watchlist.navigation.watchlistEntry

private enum class TopLevelDestination(@DrawableRes val icon: Int, @StringRes val label: Int) {
    Discover(DesignSystemR.drawable.ic_movie, R.string.nav_discover),
    Watchlist(DesignSystemR.drawable.ic_bookmark, R.string.nav_watchlist),
}

@Composable
fun MainNavigation(modifier: Modifier = Modifier, deepLinkMovieId: Long? = null, onDeepLinkConsume: () -> Unit = {}) {
    val discoverStack = rememberNavBackStack(DiscoverNavKey)
    val watchlistStack = rememberNavBackStack(WatchlistNavKey)
    var currentTab by rememberSaveable { mutableStateOf(TopLevelDestination.Discover) }

    val currentStack: NavBackStack<NavKey> = when (currentTab) {
        TopLevelDestination.Discover -> discoverStack
        TopLevelDestination.Watchlist -> watchlistStack
    }

    // Discover stays underneath so "back" from the Watchlist root returns to it. Only its root:
    // the same movie may be open in both tabs, and a key can appear once in the displayed stack.
    val displayedStack: List<NavKey> = when (currentTab) {
        TopLevelDestination.Discover -> discoverStack
        TopLevelDestination.Watchlist -> listOf(discoverStack.first()) + watchlistStack
    }

    fun navigate(key: NavKey) {
        currentStack.add(key)
    }

    fun goBack() {
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else {
            currentTab = TopLevelDestination.Discover
        }
    }

    // Release notifications are about watchlist movies, so their details open in the Watchlist tab
    val currentOnDeepLinkConsume by rememberUpdatedState(onDeepLinkConsume)
    LaunchedEffect(deepLinkMovieId) {
        val movieId = deepLinkMovieId ?: return@LaunchedEffect
        currentTab = TopLevelDestination.Watchlist
        val key = MovieDetailsNavKey(movieId)
        if (watchlistStack.lastOrNull() != key) watchlistStack.add(key)
        currentOnDeepLinkConsume()
    }

    fun selectTab(tab: TopLevelDestination) {
        if (tab == currentTab) {
            while (currentStack.size > 1) currentStack.removeLastOrNull()
        } else {
            currentTab = tab
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == currentTab,
                        onClick = { selectTab(tab) },
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        },
        // Each screen's TopAppBar handles the status bar
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        NavDisplay(
            backStack = displayedStack,
            onBack = ::goBack,
            // The bottom bar already covers the navigation bar inset; the screens must not add it again
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                discoverEntry(
                    onMovieClick = { movieId -> navigate(MovieDetailsNavKey(movieId)) },
                    onSearchClick = { navigate(SearchNavKey) },
                )
                searchEntry(
                    onMovieClick = { movieId -> navigate(MovieDetailsNavKey(movieId)) },
                    onBack = ::goBack,
                )
                watchlistEntry(onMovieClick = { movieId -> navigate(MovieDetailsNavKey(movieId)) })
                movieDetailsEntry(onBack = ::goBack)
            },
        )
    }
}
