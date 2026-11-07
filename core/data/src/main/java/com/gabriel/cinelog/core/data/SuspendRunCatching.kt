package com.gabriel.cinelog.core.data

import kotlin.coroutines.cancellation.CancellationException

// Unlike runCatching, rethrows CancellationException so cancelled coroutines actually stop
@Suppress("TooGenericExceptionCaught")
suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
