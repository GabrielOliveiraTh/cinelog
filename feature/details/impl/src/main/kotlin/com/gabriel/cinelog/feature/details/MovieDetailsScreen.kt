package com.gabriel.cinelog.feature.details

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabriel.cinelog.core.designsystem.CinelogTheme
import com.gabriel.cinelog.core.designsystem.R as DesignSystemR
import com.gabriel.cinelog.core.model.CastMember
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.MovieDetails
import com.gabriel.cinelog.core.model.WatchlistStatus
import com.gabriel.cinelog.core.ui.BackButton
import com.gabriel.cinelog.core.ui.ErrorState
import com.gabriel.cinelog.core.ui.LoadingState
import com.gabriel.cinelog.core.ui.MoviePoster
import com.gabriel.cinelog.core.ui.R as CoreUiR
import com.gabriel.cinelog.core.ui.TmdbImage
import com.gabriel.cinelog.core.ui.TmdbImageSize
import com.gabriel.cinelog.feature.details.impl.R
import java.util.Locale
import kotlinx.datetime.LocalDate

@Composable
internal fun MovieDetailsRoute(
    movieId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieDetailsViewModel = hiltViewModel<MovieDetailsViewModel, MovieDetailsViewModel.Factory>(
        creationCallback = { factory -> factory.create(movieId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val requestNotificationPermission = rememberNotificationPermissionRequest()
    MovieDetailsScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::refresh,
        onWatchlistClick = {
            // Asked when adding a movie, the moment the reason for release notifications is obvious
            val adding = (uiState as? MovieDetailsUiState.Success)?.watchlistStatus == WatchlistStatus.NotInWatchlist
            if (adding) requestNotificationPermission()
            viewModel.onWatchlistClick()
        },
        onWatchedClick = viewModel::onWatchedClick,
        modifier = modifier,
    )
}

@Composable
private fun rememberNotificationPermissionRequest(): () -> Unit {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return {}
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    return {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        // After the user denies twice, Android itself stops showing the dialog
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MovieDetailsScreen(
    uiState: MovieDetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onWatchlistClick: () -> Unit,
    onWatchedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is MovieDetailsUiState.Success) {
                        Text(uiState.details.movie.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState) {
                MovieDetailsUiState.Loading -> LoadingState()

                MovieDetailsUiState.Error -> ErrorState(stringResource(R.string.details_error), onRetry)

                is MovieDetailsUiState.Success -> MovieDetailsContent(
                    uiState,
                    onRetry,
                    onWatchlistClick,
                    onWatchedClick,
                )
            }
        }
    }
}

@Composable
private fun MovieDetailsContent(
    state: MovieDetailsUiState.Success,
    onRetry: () -> Unit,
    onWatchlistClick: () -> Unit,
    onWatchedClick: () -> Unit,
) {
    val details = state.details
    val movie = details.movie

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            TmdbImage(
                path = movie.backdropPath,
                size = TmdbImageSize.Backdrop,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
        }
        item { Header(details, Modifier.padding(16.dp)) }
        item {
            WatchlistActions(
                status = state.watchlistStatus,
                onWatchlistClick = onWatchlistClick,
                onWatchedClick = onWatchedClick,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }

        if (details.genres.isNotEmpty()) {
            item { Genres(details.genres, Modifier.padding(horizontal = 16.dp)) }
        }

        item {
            Section(title = stringResource(R.string.details_overview)) {
                Text(
                    text = movie.overview.ifBlank { stringResource(R.string.details_overview_unavailable) },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        when {
            state.isLoadingFullDetails -> item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.fullDetailsFailed -> item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.details_partial_error),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = onRetry) { Text(stringResource(CoreUiR.string.core_ui_retry)) }
                }
            }

            details.cast.isNotEmpty() -> item {
                Section(title = stringResource(R.string.details_cast), horizontalPadding = 0.dp) {
                    CastRow(details.cast)
                }
            }
        }
    }
}

@Composable
private fun Header(details: MovieDetails, modifier: Modifier = Modifier) {
    val movie = details.movie
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        MoviePoster(
            posterPath = movie.posterPath,
            contentDescription = movie.title,
            modifier = Modifier
                .width(110.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(movie.title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = metadataLine(
                    releaseDate = movie.releaseDate,
                    runtimeMinutes = details.runtimeMinutes,
                    voteAverage = movie.voteAverage,
                    locale = LocalLocale.current.platformLocale,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            details.tagline?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
private fun WatchlistActions(
    status: WatchlistStatus,
    onWatchlistClick: () -> Unit,
    onWatchedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (status == WatchlistStatus.NotInWatchlist) {
            FilledTonalButton(onClick = onWatchlistClick) {
                ButtonIcon(DesignSystemR.drawable.ic_bookmark)
                Text(stringResource(R.string.details_add_to_watchlist))
            }
        } else {
            OutlinedButton(onClick = onWatchlistClick) {
                ButtonIcon(DesignSystemR.drawable.ic_bookmark_filled)
                Text(stringResource(R.string.details_in_watchlist))
            }
            val watched = status == WatchlistStatus.Watched
            OutlinedButton(onClick = onWatchedClick) {
                ButtonIcon(
                    if (watched) DesignSystemR.drawable.ic_check_circle else DesignSystemR.drawable.ic_circle_outline,
                )
                Text(stringResource(if (watched) R.string.details_watched else R.string.details_mark_watched))
            }
        }
    }
}

@Composable
private fun ButtonIcon(@DrawableRes icon: Int) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = Modifier
            .padding(end = 8.dp)
            .size(18.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Genres(genres: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        genres.forEach { genre ->
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    text = genre,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, horizontalPadding: Dp = 16.dp, content: @Composable () -> Unit) {
    Column(Modifier.padding(top = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Box(Modifier.padding(horizontal = horizontalPadding)) { content() }
    }
}

@Composable
private fun CastRow(cast: List<CastMember>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(cast, key = { "${it.id}-${it.character}" }) { member ->
            Column(
                modifier = Modifier.width(88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TmdbImage(
                    path = member.profilePath,
                    size = TmdbImageSize.Profile,
                    contentDescription = member.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                )
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    text = member.character,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** "2008 · 2h 32min · ★ 8.5" (8,5 in pt-BR), skipping whatever is unknown. */
internal fun metadataLine(releaseDate: LocalDate?, runtimeMinutes: Int?, voteAverage: Double, locale: Locale): String =
    listOfNotNull(
        releaseDate?.year?.toString(),
        runtimeMinutes?.let(::formatRuntime),
        "★ " + String.format(locale, "%.1f", voteAverage),
    ).joinToString(" · ")

internal fun formatRuntime(minutes: Int): String {
    val hours = minutes / 60
    val remaining = minutes % 60
    return when {
        hours == 0 -> "${remaining}min"
        remaining == 0 -> "${hours}h"
        else -> "${hours}h ${remaining}min"
    }
}

@Preview
@Composable
private fun MovieDetailsScreenPreview() {
    CinelogTheme {
        MovieDetailsScreen(
            uiState = MovieDetailsUiState.Success(
                details = MovieDetails(
                    movie = Movie(
                        id = 155,
                        title = "Batman: O Cavaleiro das Trevas",
                        overview = "Batman, Gordon e Harvey Dent enfrentam o Coringa.",
                        posterPath = null,
                        backdropPath = null,
                        releaseDate = LocalDate(2008, 7, 16),
                        voteAverage = 8.5,
                    ),
                    tagline = "Bem-vindo ao mundo sem regras.",
                    runtimeMinutes = 152,
                    genres = listOf("Ação", "Crime", "Drama"),
                    cast = listOf(
                        CastMember(1, "Christian Bale", "Bruce Wayne", null),
                        CastMember(2, "Heath Ledger", "Joker", null),
                    ),
                    hasFullDetails = true,
                ),
                isLoadingFullDetails = false,
                fullDetailsFailed = false,
                watchlistStatus = WatchlistStatus.ToWatch,
            ),
            onBack = {},
            onRetry = {},
            onWatchlistClick = {},
            onWatchedClick = {},
        )
    }
}
