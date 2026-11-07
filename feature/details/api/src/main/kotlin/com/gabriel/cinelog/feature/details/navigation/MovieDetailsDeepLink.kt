package com.gabriel.cinelog.feature.details.navigation

import android.net.Uri

private const val SCHEME = "cinelog"
private const val HOST = "movie"

// In the api module so the notification that builds the link and the app that parses it share it
object MovieDetailsDeepLink {
    fun uri(movieId: Long): Uri = Uri.parse("$SCHEME://$HOST/$movieId")

    fun movieIdFrom(uri: Uri?): Long? {
        if (uri == null || uri.scheme != SCHEME || uri.host != HOST) return null
        return uri.lastPathSegment?.toLongOrNull()
    }
}
