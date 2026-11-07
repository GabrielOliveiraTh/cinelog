package com.gabriel.cinelog.core.network

import kotlinx.serialization.json.Json

internal val NetworkJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
