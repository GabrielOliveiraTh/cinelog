package com.gabriel.cinelog.feature.discover

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.gabriel.cinelog.core.designsystem.CinelogTheme
import com.gabriel.cinelog.core.designsystem.R as DesignSystemR
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.ui.ErrorState
import com.gabriel.cinelog.core.ui.LoadingState
import com.gabriel.cinelog.core.ui.MessageState
import com.gabriel.cinelog.core.ui.PagingMovieGrid
import com.gabriel.cinelog.feature.discover.impl.R
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun DiscoverRoute(
    onMovieClick: (movieId: Long) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    DiscoverScreen(
        movies = viewModel.popularMovies.collectAsLazyPagingItems(),
        onMovieClick = onMovieClick,
        onSearchClick = onSearchClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    movies: LazyPagingItems<Movie>,
    onMovieClick: (movieId: Long) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Two sources of loading state: `source` is Room, `mediator` is the network
    val loadState = movies.loadState
    val networkRefresh = loadState.mediator?.refresh ?: loadState.refresh
    val refreshError = networkRefresh as? LoadState.Error ?: loadState.source.refresh as? LoadState.Error

    // With cached movies on screen a failed refresh is not fatal: keep the list and say so
    val snackbarHostState = remember { SnackbarHostState() }
    val showingStaleData = refreshError != null && movies.itemCount > 0
    val staleDataMessage = stringResource(R.string.discover_refresh_error)
    LaunchedEffect(showingStaleData) {
        if (showingStaleData) snackbarHostState.showSnackbar(staleDataMessage)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = { Text(stringResource(R.string.discover_title)) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            painter = painterResource(DesignSystemR.drawable.ic_search),
                            contentDescription = stringResource(R.string.discover_search),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            // Only show the pull indicator over existing content; an empty screen gets the centered loader
            isRefreshing = networkRefresh is LoadState.Loading && movies.itemCount > 0,
            onRefresh = movies::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                movies.itemCount > 0 -> PagingMovieGrid(movies, onMovieClick)

                refreshError != null -> ErrorState(stringResource(R.string.discover_error), onRetry = movies::retry)

                // Before both Room and the network reach the end, zero items means the first page is still on its way
                loadState.append.endOfPaginationReached -> MessageState(stringResource(R.string.discover_empty))

                else -> LoadingState()
            }
        }
    }
}

@Preview
@Composable
private fun DiscoverScreenPreview() {
    val movies = List(6) { index ->
        Movie(
            id = index.toLong(),
            title = "Filme ${index + 1}",
            overview = "",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            voteAverage = 7.5,
        )
    }
    CinelogTheme {
        DiscoverScreen(
            movies = flowOf(PagingData.from(movies)).collectAsLazyPagingItems(),
            onMovieClick = {},
            onSearchClick = {},
        )
    }
}
