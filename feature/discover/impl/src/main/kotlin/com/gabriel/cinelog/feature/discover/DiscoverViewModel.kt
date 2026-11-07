package com.gabriel.cinelog.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gabriel.cinelog.core.data.MovieRepository
import com.gabriel.cinelog.core.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class DiscoverViewModel @Inject constructor(movieRepository: MovieRepository) : ViewModel() {

    val popularMovies: Flow<PagingData<Movie>> = movieRepository
        .popularMovies()
        .cachedIn(viewModelScope)
}
