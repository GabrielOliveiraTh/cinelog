package com.gabriel.cinelog.core.data.di

import com.gabriel.cinelog.core.data.LocalWatchlistRepository
import com.gabriel.cinelog.core.data.MovieRepository
import com.gabriel.cinelog.core.data.OfflineFirstMovieRepository
import com.gabriel.cinelog.core.data.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindsMovieRepository(repository: OfflineFirstMovieRepository): MovieRepository

    @Binds
    @Singleton
    fun bindsWatchlistRepository(repository: LocalWatchlistRepository): WatchlistRepository
}
