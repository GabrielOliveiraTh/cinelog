package com.gabriel.cinelog.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.cinelog.core.data.MovieRepository
import com.gabriel.cinelog.core.data.WatchlistRepository
import com.gabriel.cinelog.core.data.suspendRunCatching
import com.gabriel.cinelog.core.model.MovieDetails
import com.gabriel.cinelog.core.model.WatchlistStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MovieDetailsViewModel.Factory::class)
class MovieDetailsViewModel @AssistedInject constructor(
    @Assisted private val movieId: Long,
    private val movieRepository: MovieRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private val refreshStatus = MutableStateFlow(RefreshStatus.Loading)

    val uiState: StateFlow<MovieDetailsUiState> = combine(
        movieRepository.observeMovieDetails(movieId),
        refreshStatus,
        watchlistRepository.observeStatus(movieId),
    ) { details, status, watchlistStatus ->
        when {
            details != null -> MovieDetailsUiState.Success(
                details = details,
                isLoadingFullDetails = !details.hasFullDetails && status == RefreshStatus.Loading,
                fullDetailsFailed = !details.hasFullDetails && status == RefreshStatus.Failed,
                watchlistStatus = watchlistStatus,
            )

            status == RefreshStatus.Failed -> MovieDetailsUiState.Error

            else -> MovieDetailsUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MovieDetailsUiState.Loading,
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshStatus.value = RefreshStatus.Loading
            val result = suspendRunCatching { movieRepository.refreshMovieDetails(movieId) }
            refreshStatus.value = if (result.isSuccess) RefreshStatus.Done else RefreshStatus.Failed
        }
    }

    fun onWatchlistClick() {
        val status = currentWatchlistStatus() ?: return
        viewModelScope.launch {
            if (status == WatchlistStatus.NotInWatchlist) {
                watchlistRepository.add(movieId)
            } else {
                watchlistRepository.remove(movieId)
            }
        }
    }

    fun onWatchedClick() {
        val status = currentWatchlistStatus() ?: return
        if (status == WatchlistStatus.NotInWatchlist) return
        viewModelScope.launch {
            watchlistRepository.setWatched(movieId, watched = status != WatchlistStatus.Watched)
        }
    }

    private fun currentWatchlistStatus(): WatchlistStatus? =
        (uiState.value as? MovieDetailsUiState.Success)?.watchlistStatus

    @AssistedFactory
    interface Factory {
        fun create(movieId: Long): MovieDetailsViewModel
    }

    private enum class RefreshStatus { Loading, Done, Failed }
}

sealed interface MovieDetailsUiState {
    data object Loading : MovieDetailsUiState

    data object Error : MovieDetailsUiState

    data class Success(
        val details: MovieDetails,
        val isLoadingFullDetails: Boolean,
        val fullDetailsFailed: Boolean,
        val watchlistStatus: WatchlistStatus,
    ) : MovieDetailsUiState
}
