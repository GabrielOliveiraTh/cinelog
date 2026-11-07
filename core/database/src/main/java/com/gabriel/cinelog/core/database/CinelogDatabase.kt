package com.gabriel.cinelog.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gabriel.cinelog.core.database.dao.MovieDao
import com.gabriel.cinelog.core.database.dao.MovieDetailsDao
import com.gabriel.cinelog.core.database.dao.PopularMovieDao
import com.gabriel.cinelog.core.database.dao.WatchlistDao
import com.gabriel.cinelog.core.database.model.CastMemberEntity
import com.gabriel.cinelog.core.database.model.MovieDetailsEntity
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.database.model.PopularMovieEntity
import com.gabriel.cinelog.core.database.model.RemoteKeyEntity
import com.gabriel.cinelog.core.database.model.WatchlistEntity

@Database(
    entities = [
        MovieEntity::class,
        PopularMovieEntity::class,
        RemoteKeyEntity::class,
        MovieDetailsEntity::class,
        CastMemberEntity::class,
        WatchlistEntity::class,
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ],
)
@TypeConverters(Converters::class)
abstract class CinelogDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    abstract fun popularMovieDao(): PopularMovieDao

    abstract fun movieDetailsDao(): MovieDetailsDao

    abstract fun watchlistDao(): WatchlistDao
}
