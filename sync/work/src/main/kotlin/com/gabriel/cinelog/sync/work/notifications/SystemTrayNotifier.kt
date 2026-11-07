package com.gabriel.cinelog.sync.work.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.feature.details.navigation.MovieDetailsDeepLink
import com.gabriel.cinelog.sync.work.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val RELEASES_CHANNEL_ID = "releases"
private const val RELEASES_GROUP = "com.gabriel.cinelog.RELEASES"

internal class SystemTrayNotifier @Inject constructor(@ApplicationContext private val context: Context) : Notifier {

    @SuppressLint("MissingPermission") // checked by hasPermission()
    override fun notifyReleased(movies: List<Movie>): Boolean {
        if (movies.isEmpty()) return true
        if (!hasPermission()) return false

        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(RELEASES_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.sync_releases_channel_name))
                .setDescription(context.getString(R.string.sync_releases_channel_description))
                .build(),
        )

        movies.forEach { movie ->
            val notification = NotificationCompat.Builder(context, RELEASES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.sync_release_title, movie.title))
                .setContentText(context.getString(R.string.sync_release_text))
                .setContentIntent(openDetailsIntent(movie.id))
                .setAutoCancel(true)
                .setGroup(RELEASES_GROUP)
                .build()
            // The movie id as notification id: a second sync updates the same notification instead of duplicating it
            manager.notify(movie.id.toInt(), notification)
        }
        return true
    }

    private fun hasPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    private fun openDetailsIntent(movieId: Long): PendingIntent = PendingIntent.getActivity(
        context,
        movieId.toInt(),
        Intent(Intent.ACTION_VIEW, MovieDetailsDeepLink.uri(movieId)).setPackage(context.packageName),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
