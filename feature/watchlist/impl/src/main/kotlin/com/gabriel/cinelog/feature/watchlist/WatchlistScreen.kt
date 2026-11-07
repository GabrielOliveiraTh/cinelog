package com.gabriel.cinelog.feature.watchlist

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabriel.cinelog.core.designsystem.CinelogTheme
import com.gabriel.cinelog.core.designsystem.R as DesignSystemR
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.WatchlistItem
import com.gabriel.cinelog.core.ui.LoadingState
import com.gabriel.cinelog.core.ui.MessageState
import com.gabriel.cinelog.core.ui.MoviePoster
import com.gabriel.cinelog.feature.watchlist.impl.R
import kotlinx.coroutines.launch

@Composable
internal fun WatchlistRoute(
    onMovieClick: (movieId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WatchlistScreen(
        uiState = uiState,
        onMovieClick = onMovieClick,
        onWatchedToggle = viewModel::onToggleWatched,
        onRemove = viewModel::onRemove,
        onUndoRemove = viewModel::onUndoRemove,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WatchlistScreen(
    uiState: WatchlistUiState,
    onMovieClick: (movieId: Long) -> Unit,
    onWatchedToggle: (WatchlistItem) -> Unit,
    onRemove: (WatchlistItem) -> Unit,
    onUndoRemove: (WatchlistItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Strings are read inside the coroutine, outside composition, so take them from Resources
    val resources = LocalResources.current

    val removeWithUndo: (WatchlistItem) -> Unit = { item ->
        onRemove(item)
        scope.launch {
            // Only the latest removal can be undone
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = resources.getString(R.string.watchlist_removed, item.movie.title),
                actionLabel = resources.getString(R.string.watchlist_undo),
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) onUndoRemove(item)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.watchlist_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            WatchlistUiState.Loading -> LoadingState(contentModifier)

            WatchlistUiState.Empty -> MessageState(stringResource(R.string.watchlist_empty), contentModifier)

            is WatchlistUiState.Success -> LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                section(
                    title = R.string.watchlist_to_watch,
                    items = uiState.toWatch,
                    onMovieClick = onMovieClick,
                    onWatchedToggle = onWatchedToggle,
                    onRemove = removeWithUndo,
                )
                section(
                    title = R.string.watchlist_watched,
                    items = uiState.watched,
                    onMovieClick = onMovieClick,
                    onWatchedToggle = onWatchedToggle,
                    onRemove = removeWithUndo,
                )
            }
        }
    }
}

private fun LazyListScope.section(
    @StringRes title: Int,
    items: List<WatchlistItem>,
    onMovieClick: (movieId: Long) -> Unit,
    onWatchedToggle: (WatchlistItem) -> Unit,
    onRemove: (WatchlistItem) -> Unit,
) {
    if (items.isEmpty()) return
    item(key = "header-$title", contentType = "header") {
        Text(
            text = stringResource(title, items.size),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        )
    }
    items(items, key = { it.movie.id }, contentType = { "movie" }) { item ->
        WatchlistRow(
            item = item,
            onClick = { onMovieClick(item.movie.id) },
            onWatchedToggle = { onWatchedToggle(item) },
            onRemove = { onRemove(item) },
            // Rows slide to the other section instead of jumping when marked as watched
            modifier = Modifier.animateItem(),
        )
    }
}

@Composable
private fun WatchlistRow(
    item: WatchlistItem,
    onClick: () -> Unit,
    onWatchedToggle: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val movie = item.movie
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MoviePoster(
            posterPath = movie.posterPath,
            contentDescription = null,
            modifier = Modifier
                .width(56.dp)
                .clip(RoundedCornerShape(6.dp)),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            movie.releaseDate?.let {
                Text(
                    text = it.year.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onWatchedToggle) {
            if (item.isWatched) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_check_circle),
                    contentDescription = stringResource(R.string.watchlist_mark_unwatched),
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_circle_outline),
                    contentDescription = stringResource(R.string.watchlist_mark_watched),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(DesignSystemR.drawable.ic_delete),
                contentDescription = stringResource(R.string.watchlist_remove),
            )
        }
    }
}

@Preview
@Composable
private fun WatchlistScreenPreview() {
    fun item(id: Long, title: String, watched: Boolean) = WatchlistItem(
        movie = Movie(id, title, "", null, null, null, 8.0),
        isWatched = watched,
        addedAtMillis = id,
    )
    CinelogTheme {
        WatchlistScreen(
            uiState = WatchlistUiState.Success(
                toWatch = listOf(item(1, "Duna: Parte Dois", false), item(2, "Oppenheimer", false)),
                watched = listOf(item(3, "Interestelar", true)),
            ),
            onMovieClick = {},
            onWatchedToggle = {},
            onRemove = {},
            onUndoRemove = {},
        )
    }
}
