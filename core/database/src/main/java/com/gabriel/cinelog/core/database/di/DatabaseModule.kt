package com.gabriel.cinelog.core.database.di

import android.content.Context
import androidx.room.Room
import com.gabriel.cinelog.core.database.CinelogDatabase
import com.gabriel.cinelog.core.database.dao.MovieDao
import com.gabriel.cinelog.core.database.dao.MovieDetailsDao
import com.gabriel.cinelog.core.database.dao.PopularMovieDao
import com.gabriel.cinelog.core.database.dao.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CinelogDatabase =
        Room.databaseBuilder(context, CinelogDatabase::class.java, "cinelog.db").build()

    @Provides
    fun provideMovieDao(database: CinelogDatabase): MovieDao = database.movieDao()

    @Provides
    fun providePopularMovieDao(database: CinelogDatabase): PopularMovieDao = database.popularMovieDao()

    @Provides
    fun provideMovieDetailsDao(database: CinelogDatabase): MovieDetailsDao = database.movieDetailsDao()

    @Provides
    fun provideWatchlistDao(database: CinelogDatabase): WatchlistDao = database.watchlistDao()
}
