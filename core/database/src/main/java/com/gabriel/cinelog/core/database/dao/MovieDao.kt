package com.gabriel.cinelog.core.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.gabriel.cinelog.core.database.model.MovieEntity

@Dao
interface MovieDao {
    // REPLACE would delete and re-insert, firing foreign key cascades
    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)
}
