package com.gabriel.cinelog.feature.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.gabriel.cinelog.core.designsystem.CinelogTheme
import com.gabriel.cinelog.core.designsystem.R as DesignSystemR
import com.gabriel.cinelog.core.model.Movie
import com.gabriel.cinelog.core.ui.BackButton
import com.gabriel.cinelog.core.ui.ErrorState
import com.gabriel.cinelog.core.ui.LoadingState
import com.gabriel.cinelog.core.ui.MessageState
import com.gabriel.cinelog.core.ui.PagingMovieGrid
import com.gabriel.cinelog.feature.search.impl.R
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun SearchRoute(
    onMovieClick: (movieId: Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchedQuery by viewModel.searchedQuery.collectAsStateWithLifecycle()
    SearchScreen(
        query = query,
        searchedQuery = searchedQuery,
        results = viewModel.results.collectAsLazyPagingItems(),
        onQueryChange = viewModel::onQueryChange,
        onMovieClick = onMovieClick,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    query: String,
    searchedQuery: String,
    results: LazyPagingItems<Movie>,
    onQueryChange: (String) -> Unit,
    onMovieClick: (movieId: Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(onClick = onBack) },
                title = { SearchField(query = query, onQueryChange = onQueryChange) },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        val trimmedQuery = query.trim()
        val refresh = results.loadState.refresh

        when {
            trimmedQuery.length < MIN_QUERY_LENGTH -> MessageState(
                stringResource(R.string.search_hint),
                contentModifier,
            )

            // Keep showing the previous results while the next ones load
            results.itemCount > 0 -> PagingMovieGrid(results, onMovieClick, contentModifier)

            // Still inside the debounce window: the search for this text has not even started
            trimmedQuery != searchedQuery -> LoadingState(contentModifier)

            refresh is LoadState.Error -> ErrorState(
                stringResource(R.string.search_error),
                results::retry,
                contentModifier,
            )

            refresh is LoadState.NotLoading && results.loadState.append.endOfPaginationReached ->
                MessageState(stringResource(R.string.search_no_results, searchedQuery), contentModifier)

            else -> LoadingState(contentModifier)
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Coming back from a result (query already filled) should not pop the keyboard again
    LaunchedEffect(Unit) {
        if (query.isEmpty()) focusRequester.requestFocus()
    }

    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // Results already update while typing
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_close),
                        contentDescription = stringResource(R.string.search_clear),
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
    )
}

@Preview
@Composable
private fun SearchScreenPreview() {
    val movies = List(4) { index ->
        Movie(
            id = index.toLong(),
            title = "Batman ${index + 1}",
            overview = "",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            voteAverage = 7.0,
        )
    }
    CinelogTheme {
        SearchScreen(
            query = "batman",
            searchedQuery = "batman",
            results = flowOf(PagingData.from(movies)).collectAsLazyPagingItems(),
            onQueryChange = {},
            onMovieClick = {},
            onBack = {},
        )
    }
}
