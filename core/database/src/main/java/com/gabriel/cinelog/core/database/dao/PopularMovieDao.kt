package com.gabriel.cinelog.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.gabriel.cinelog.core.database.model.MovieEntity
import com.gabriel.cinelog.core.database.model.PopularMovieEntity
import com.gabriel.cinelog.core.database.model.RemoteKeyEntity

// Abstract class so the multi-step write can be a single @Transaction
@Dao
abstract class PopularMovieDao {

    @Query(
        """
        SELECT movies.* FROM movies
        INNER JOIN popular_movies ON movies.id = popular_movies.movie_id
        ORDER BY popular_movies.position
        """,
    )
    abstract fun pagingSource(): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM remote_keys WHERE list_name = '$POPULAR_LIST'")
    abstract suspend fun remoteKey(): RemoteKeyEntity?

    @Transaction
    open suspend fun savePage(
        movies: List<MovieEntity>,
        firstPosition: Int,
        nextPage: Int?,
        replace: Boolean,
        updatedAt: Long,
    ) {
        if (replace) clearPositions()
        upsertMovies(movies)
        insertPositions(movies.mapIndexed { index, movie -> PopularMovieEntity(movie.id, firstPosition + index) })
        upsertRemoteKey(RemoteKeyEntity(POPULAR_LIST, nextPage, updatedAt))
    }

    @Upsert
    protected abstract suspend fun upsertMovies(movies: List<MovieEntity>)

    // IGNORE keeps the first position of a movie TMDB repeats on a later page
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertPositions(positions: List<PopularMovieEntity>)

    @Upsert
    protected abstract suspend fun upsertRemoteKey(key: RemoteKeyEntity)

    @Query("DELETE FROM popular_movies")
    protected abstract suspend fun clearPositions()

    private companion object {
        const val POPULAR_LIST = "popular"
    }
}
