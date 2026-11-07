package com.gabriel.cinelog.sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val MAX_ATTEMPTS = 3

@HiltWorker
internal class WatchlistSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncer: WatchlistSyncer,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = when {
        syncer.sync() -> Result.success()
        runAttemptCount < MAX_ATTEMPTS -> Result.retry()
        else -> Result.failure()
    }
}
