package com.gabriel.cinelog.sync.work.notifications

import com.gabriel.cinelog.core.model.Movie

interface Notifier {
    /** @return false when notifications are not allowed (permission denied), so nothing was shown. */
    fun notifyReleased(movies: List<Movie>): Boolean
}
