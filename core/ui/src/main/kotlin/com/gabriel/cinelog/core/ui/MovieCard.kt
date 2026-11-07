package com.gabriel.cinelog.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabriel.cinelog.core.designsystem.CinelogTheme
import com.gabriel.cinelog.core.designsystem.Spacing
import com.gabriel.cinelog.core.model.Movie

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "cardScale",
    )
    val dark = isSystemInDarkTheme()

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = if (dark) 0.dp else 1.dp),
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        Column {
            MoviePoster(
                posterPath = movie.posterPath,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxWidth(),
            )
            Column(
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "★ " + String.format(LocalLocale.current.platformLocale, "%.1f", movie.voteAverage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Fixed 2:3 ratio reserves the space before the image loads, so the grid does not jump
@Composable
fun MoviePoster(posterPath: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    TmdbImage(
        path = posterPath,
        size = TmdbImageSize.Poster,
        contentDescription = contentDescription,
        modifier = modifier.aspectRatio(2f / 3f),
        placeholder = {
            Text(
                text = contentDescription.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(8.dp),
            )
        },
    )
}

@Preview
@Composable
private fun MovieCardPreview() {
    CinelogTheme {
        MovieCard(
            movie = Movie(
                id = 550,
                title = "Clube da Luta",
                overview = "",
                posterPath = null,
                backdropPath = null,
                releaseDate = null,
                voteAverage = 8.4,
            ),
            onClick = {},
            modifier = Modifier.width(140.dp),
        )
    }
}
