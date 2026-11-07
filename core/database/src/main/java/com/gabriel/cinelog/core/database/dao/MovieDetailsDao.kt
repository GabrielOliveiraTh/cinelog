package com.gabriel.cinelog.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.gabriel.cinelog.core.database.model.CastMemberEntity
import com.gabriel.cinelog.core.database.model.MovieDetailsEntity
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.database.model.PopulatedMovieDetails
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MovieDetailsDao {

    @Transaction
    @Query("SELECT * FROM movies WHERE id = :movieId")
    abstract fun observeMovieDetails(movieId: Long): Flow<PopulatedMovieDetails?>

    @Transaction
    open suspend fun saveDetails(movie: MovieEntity, details: MovieDetailsEntity, cast: List<CastMemberEntity>) {
        upsertMovie(movie)
        upsertDetails(details)
        deleteCast(movie.id)
        insertCast(cast)
    }

    @Upsert
    protected abstract suspend fun upsertMovie(movie: MovieEntity)

    @Upsert
    protected abstract suspend fun upsertDetails(details: MovieDetailsEntity)

    @Query("DELETE FROM movie_cast WHERE movie_id = :movieId")
    protected abstract suspend fun deleteCast(movieId: Long)

    @Insert
    protected abstract suspend fun insertCast(cast: List<CastMemberEntity>)
}
