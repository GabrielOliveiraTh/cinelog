package com.gabriel.cinelog.feature.details.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Only the id: whole objects in the back stack break when it is restored after process death
@Serializable
data class MovieDetailsNavKey(val movieId: Long) : NavKey
