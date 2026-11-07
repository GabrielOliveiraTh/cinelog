package com.gabriel.cinelog.core.data.paging

import com.gabriel.cinelog.core.network.model.NetworkPagedResponse

internal const val TMDB_PAGE_SIZE = 20

// TMDB reports more pages than it serves: requests above page 500 fail
internal const val TMDB_MAX_PAGE = 500

internal fun NetworkPagedResponse<*>.nextPage(): Int? =
    (page + 1).takeIf { page < minOf(totalPages, TMDB_MAX_PAGE) && results.isNotEmpty() }
