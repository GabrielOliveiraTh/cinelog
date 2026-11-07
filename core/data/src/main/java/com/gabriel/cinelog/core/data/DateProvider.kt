package com.gabriel.cinelog.core.data

import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

fun interface DateProvider {
    fun today(): LocalDate
}

class SystemDateProvider @Inject constructor() : DateProvider {
    @OptIn(ExperimentalTime::class)
    override fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
