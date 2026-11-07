package com.gabriel.cinelog.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

private const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

// The smallest width that fits saves data and memory
enum class TmdbImageSize(internal val pathSegment: String) {
    Poster("w342"),
    Backdrop("w780"),
    Profile("w185"),
}

// The background keeps the layout's shape while loading or when there is no image
@Composable
fun TmdbImage(
    path: String?,
    size: TmdbImageSize,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (path == null) {
            placeholder()
        } else {
            AsyncImage(
                model = tmdbImageUrl(path, size),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

fun tmdbImageUrl(path: String?, size: TmdbImageSize): String? =
    path?.let { "$TMDB_IMAGE_BASE_URL${size.pathSegment}$it" }
