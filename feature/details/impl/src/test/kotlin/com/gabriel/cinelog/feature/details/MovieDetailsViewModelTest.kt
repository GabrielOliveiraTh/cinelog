package com.gabriel.cinelog.feature.details

import androidx.paging.PagingData
import com.gabriel.cinelog.core.data.MovieRepository
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.MovieDetails
import com.gabriel.cinelog.core.model.WatchlistStatus
import com.gabriel.cinelog.core.testing.MainDispatcherRule
import com.gabriel.cinelog.core.testing.TestWatchlistRepository
import com.gabriel.cinelog.core.testing.collectInBackground
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MovieDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()
    private val watchlistRepository = TestWatchlistRepository()

    private fun createViewModel() = MovieDetailsViewModel(
        movieId = MOVIE_ID,
        movieRepository = repository,
        watchlistRepository = watchlistRepository,
    )

    @Test
    fun `unknown movie shows loading, then the fetched details`() = runTest {
        val viewModel = createViewModel()
        collectInBackground(viewModel.uiState)
        assertEquals(MovieDetailsUiState.Loading, viewModel.uiState.value)

        repository.completeRefresh()

        val state = viewModel.uiState.value as MovieDetailsUiState.Success
        assertTrue(state.details.hasFullDetails)
        assertEquals(false, state.isLoadingFullDetails)
    }

    @Test
    fun `cached list data is shown immediately while the rest loads`() = runTest {
        repository.details.value = partialDetails

        val viewModel = createViewModel()
        collectInBackground(viewModel.uiState)

        val state = viewModel.uiState.value as MovieDetailsUiState.Success
        assertEquals("Clube da Luta", state.details.movie.title)
        assertTrue(state.isLoadingFullDetails)
    }

    @Test
    fun `failure without cached data shows the error, and retry recovers`() = runTest {
        val viewModel = createViewModel()
        collectInBackground(viewModel.uiState)

        repository.failRefresh()
        assertEquals(MovieDetailsUiState.Error, viewModel.uiState.value)

        viewModel.refresh()
        assertEquals(MovieDetailsUiState.Loading, viewModel.uiState.value)
        repository.completeRefresh()
        assertTrue(viewModel.uiState.value is MovieDetailsUiState.Success)
    }

    @Test
    fun `failure with cached data keeps it on screen`() = runTest {
        repository.details.value = partialDetails
        val viewModel = createViewModel()
        collectInBackground(viewModel.uiState)

        repository.failRefresh()

        val state = viewModel.uiState.value as MovieDetailsUiState.Success
        assertTrue(state.fullDetailsFailed)
        assertEquals("Clube da Luta", state.details.movie.title)
    }

    @Test
    fun `watchlist buttons add, mark as watched and remove the movie`() = runTest {
        repository.details.value = partialDetails
        val viewModel = createViewModel()
        collectInBackground(viewModel.uiState)
        fun status() = (viewModel.uiState.value as MovieDetailsUiState.Success).watchlistStatus

        assertEquals(WatchlistStatus.NotInWatchlist, status())
        // Marking as watched is only possible once the movie is in the list
        viewModel.onWatchedClick()
        assertEquals(WatchlistStatus.NotInWatchlist, status())

        viewModel.onWatchlistClick()
        assertEquals(WatchlistStatus.ToWatch, status())

        viewModel.onWatchedClick()
        assertEquals(WatchlistStatus.Watched, status())

        viewModel.onWatchlistClick()
        assertEquals(WatchlistStatus.NotInWatchlist, status())
    }

    private companion object {
        const val MOVIE_ID = 550L

        val partialDetails = MovieDetails(
            movie = Movie(
                id = MOVIE_ID,
                title = "Clube da Luta",
                overview = "",
                posterPath = null,
                backdropPath = null,
                releaseDate = null,
                voteAverage = 8.4,
            ),
            tagline = null,
            runtimeMinutes = null,
            genres = emptyList(),
            cast = emptyList(),
            hasFullDetails = false,
        )
    }

    /** Each refresh suspends until the test decides how it ends. */
    private class FakeMovieRepository : MovieRepository {
        val details = MutableStateFlow<MovieDetails?>(null)
        private var pendingRefresh = CompletableDeferred<Boolean>()

        fun completeRefresh() {
            pendingRefresh.complete(true)
        }

        fun failRefresh() {
            pendingRefresh.complete(false)
        }

        override fun popularMovies(): Flow<PagingData<Movie>> = emptyFlow()

        override fun searchMovies(query: String): Flow<PagingData<Movie>> = emptyFlow()

        override fun observeMovieDetails(movieId: Long): Flow<MovieDetails?> = details

        override suspend fun refreshMovieDetails(movieId: Long) {
            val succeeded = pendingRefresh.await()
            pendingRefresh = CompletableDeferred()
            if (!succeeded) throw IOException("offline")
            details.value = (details.value ?: partialDetails).copy(
                runtimeMinutes = 139,
                genres = listOf("Drama"),
                hasFullDetails = true,
            )
        }
    }
}
