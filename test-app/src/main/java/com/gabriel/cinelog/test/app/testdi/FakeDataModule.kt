/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gabriel.cinelog.test.app.testdi

import androidx.paging.PagingData
import com.gabriel.cinelog.core.data.MovieRepository
import com.gabriel.cinelog.core.data.WatchlistRepository
import com.gabriel.cinelog.core.data.di.DataModule
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.MovieDetails
import com.gabriel.cinelog.core.testing.TestWatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
interface FakeDataModule {

    @Binds
    fun bindRepository(fakeRepository: FakeMovieRepository): MovieRepository

    @Binds
    fun bindWatchlistRepository(repository: TestWatchlistRepository): WatchlistRepository
}

class FakeMovieRepository @Inject constructor() : MovieRepository {
    override fun popularMovies(): Flow<PagingData<Movie>> = flowOf(PagingData.from(fakeMovies))

    override fun searchMovies(query: String): Flow<PagingData<Movie>> =
        flowOf(PagingData.from(fakeMovies.filter { it.title.contains(query, ignoreCase = true) }))

    override fun observeMovieDetails(movieId: Long): Flow<MovieDetails?> = flowOf(
        fakeMovies.firstOrNull { it.id == movieId }?.let { movie ->
            MovieDetails(
                movie = movie,
                tagline = null,
                runtimeMinutes = 120,
                genres = listOf("Drama"),
                cast = emptyList(),
                hasFullDetails = true,
            )
        },
    )

    override suspend fun refreshMovieDetails(movieId: Long) = Unit
}

val fakeMovies = listOf("Clube da Luta", "Interestelar", "Parasita").mapIndexed { index, title ->
    Movie(
        id = index.toLong(),
        title = title,
        overview = "",
        posterPath = null,
        backdropPath = null,
        releaseDate = null,
        voteAverage = 8.0,
    )
}
