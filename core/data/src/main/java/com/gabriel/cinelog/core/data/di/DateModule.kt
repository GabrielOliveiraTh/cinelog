package com.gabriel.cinelog.core.data.di

import com.gabriel.cinelog.core.data.DateProvider
import com.gabriel.cinelog.core.data.SystemDateProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Separate from DataModule, which instrumented tests replace
@Module
@InstallIn(SingletonComponent::class)
interface DateModule {
    @Binds
    fun bindsDateProvider(provider: SystemDateProvider): DateProvider
}
