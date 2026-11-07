package com.gabriel.cinelog.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gabriel.cinelog.core.data.MovieRepository
import com.gabriel.cinelog.core.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal const val MIN_QUERY_LENGTH = 2
internal const val SEARCH_DEBOUNCE_MILLIS = 300L
private const val QUERY_KEY = "query"

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // SavedStateHandle keeps the text across process death, not only rotation
    val query: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_KEY, "")

    val searchedQuery: StateFlow<String> = query
        .map { it.trim() }
        // Clearing the field (short query) should clear the results right away, not after the delay
        .debounce { if (it.length < MIN_QUERY_LENGTH) 0L else SEARCH_DEBOUNCE_MILLIS }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // A query restored after process death is searched immediately
            initialValue = query.value.trim(),
        )

    // flatMapLatest cancels the previous search, so a slow "bat" response never overwrites "batman"
    val results: Flow<PagingData<Movie>> = searchedQuery
        .flatMapLatest { query ->
            if (query.length < MIN_QUERY_LENGTH) {
                flowOf(PagingData.empty())
            } else {
                movieRepository.searchMovies(query)
            }
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) {
        savedStateHandle[QUERY_KEY] = query
    }
}
