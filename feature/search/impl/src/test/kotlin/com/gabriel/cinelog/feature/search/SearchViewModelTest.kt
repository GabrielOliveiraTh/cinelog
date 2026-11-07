package com.gabriel.cinelog.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.gabriel.cinelog.core.data.MovieRepository
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.model.MovieDetails
import com.gabriel.cinelog.core.testing.MainDispatcherRule
import com.gabriel.cinelog.core.testing.collectInBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = RecordingMovieRepository()

    private fun TestScope.createViewModel(savedState: Map<String, Any> = emptyMap()): SearchViewModel {
        val viewModel = SearchViewModel(repository, SavedStateHandle(savedState))
        collectInBackground(viewModel.results)
        return viewModel
    }

    @Test
    fun `typing quickly searches only the final text`() = runTest {
        val viewModel = createViewModel()

        listOf("b", "ba", "bat", "batm", "batma", "batman").forEach { text ->
            viewModel.onQueryChange(text)
            advanceTimeBy(100)
        }
        assertEquals(emptyList<String>(), repository.searchedQueries)

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + 1)
        assertEquals(listOf("batman"), repository.searchedQueries)
        assertEquals("batman", viewModel.searchedQuery.value)
    }

    @Test
    fun `short queries never reach the repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("b")
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS * 2)

        assertEquals(emptyList<String>(), repository.searchedQueries)
    }

    @Test
    fun `surrounding spaces do not trigger a new search`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("matrix")
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + 1)
        viewModel.onQueryChange("matrix ")
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + 1)

        assertEquals(listOf("matrix"), repository.searchedQueries)
    }

    @Test
    fun `query restored after process death is searched without waiting`() = runTest {
        val viewModel = createViewModel(savedState = mapOf("query" to "duna"))

        assertEquals("duna", viewModel.query.value)
        assertEquals(listOf("duna"), repository.searchedQueries)
    }

    private class RecordingMovieRepository : MovieRepository {
        val searchedQueries = mutableListOf<String>()

        override fun searchMovies(query: String): Flow<PagingData<Movie>> {
            searchedQueries += query
            return flowOf(PagingData.empty())
        }

        override fun popularMovies(): Flow<PagingData<Movie>> = emptyFlow()

        override fun observeMovieDetails(movieId: Long): Flow<MovieDetails?> = emptyFlow()

        override suspend fun refreshMovieDetails(movieId: Long) = Unit
    }
}
