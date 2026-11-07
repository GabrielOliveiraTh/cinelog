package com.gabriel.cinelog.feature.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.cinelog.core.data.WatchlistRepository
import com.gabriel.cinelog.core.model.WatchlistItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WatchlistViewModel @Inject constructor(private val watchlistRepository: WatchlistRepository) : ViewModel() {

    val uiState: StateFlow<WatchlistUiState> = watchlistRepository.observeWatchlist()
        .map { items ->
            if (items.isEmpty()) {
                WatchlistUiState.Empty
            } else {
                val (watched, toWatch) = items.partition(WatchlistItem::isWatched)
                WatchlistUiState.Success(toWatch = toWatch, watched = watched)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WatchlistUiState.Loading,
        )

    fun onToggleWatched(item: WatchlistItem) {
        viewModelScope.launch { watchlistRepository.setWatched(item.movie.id, watched = !item.isWatched) }
    }

    // No one-off "show snackbar" event: the screen keeps the removed item and calls onUndoRemove
    fun onRemove(item: WatchlistItem) {
        viewModelScope.launch { watchlistRepository.remove(item.movie.id) }
    }

    fun onUndoRemove(item: WatchlistItem) {
        viewModelScope.launch { watchlistRepository.restore(item) }
    }
}

sealed interface WatchlistUiState {
    data object Loading : WatchlistUiState

    data object Empty : WatchlistUiState

    data class Success(val toWatch: List<WatchlistItem>, val watched: List<WatchlistItem>) : WatchlistUiState
}
