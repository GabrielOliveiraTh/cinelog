package com.gabriel.cinelog.feature.watchlist

import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.testing.MainDispatcherRule
import com.gabriel.cinelog.core.testing.TestWatchlistRepository
import com.gabriel.cinelog.core.testing.collectInBackground
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WatchlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestWatchlistRepository().apply { registerMovies(dune, oppenheimer, interstellar) }

    private fun TestScope.createViewModel(): WatchlistViewModel {
        val viewModel = WatchlistViewModel(repository)
        collectInBackground(viewModel.uiState)
        return viewModel
    }

    @Test
    fun `empty list shows the empty state`() = runTest {
        val viewModel = createViewModel()

        assertEquals(WatchlistUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `movies are split into to watch and watched, newest first`() = runTest {
        repository.add(dune.id)
        repository.add(oppenheimer.id)
        repository.add(interstellar.id)
        repository.setWatched(interstellar.id, watched = true)

        val state = createViewModel().uiState.value as WatchlistUiState.Success

        assertEquals(listOf(oppenheimer, dune), state.toWatch.map { it.movie })
        assertEquals(listOf(interstellar), state.watched.map { it.movie })
    }

    @Test
    fun `toggling watched moves the movie between sections`() = runTest {
        repository.add(dune.id)
        val viewModel = createViewModel()

        val item = (viewModel.uiState.value as WatchlistUiState.Success).toWatch.single()
        viewModel.onToggleWatched(item)

        val state = viewModel.uiState.value as WatchlistUiState.Success
        assertEquals(emptyList<Movie>(), state.toWatch.map { it.movie })
        assertEquals(listOf(dune), state.watched.map { it.movie })
    }

    @Test
    fun `undo puts a removed movie back in its original position`() = runTest {
        repository.add(dune.id)
        repository.add(oppenheimer.id)
        repository.add(interstellar.id)
        val viewModel = createViewModel()
        val removed = (viewModel.uiState.value as WatchlistUiState.Success).toWatch[1]

        viewModel.onRemove(removed)
        assertEquals(
            listOf(interstellar, dune),
            (viewModel.uiState.value as WatchlistUiState.Success).toWatch.map { it.movie },
        )

        viewModel.onUndoRemove(removed)
        assertEquals(
            listOf(interstellar, oppenheimer, dune),
            (viewModel.uiState.value as WatchlistUiState.Success).toWatch.map { it.movie },
        )
    }

    private companion object {
        fun movie(id: Long, title: String) = Movie(id, title, "", null, null, null, 8.0)

        val dune = movie(1, "Duna: Parte Dois")
        val oppenheimer = movie(2, "Oppenheimer")
        val interstellar = movie(3, "Interestelar")
    }
}
