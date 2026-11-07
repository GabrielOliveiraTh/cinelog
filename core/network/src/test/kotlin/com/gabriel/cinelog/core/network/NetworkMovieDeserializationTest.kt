package com.gabriel.cinelog.core.network

import com.gabriel.cinelog.core.network.model.NetworkMovie
import com.gabriel.cinelog.core.network.model.NetworkPagedResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkMovieDeserializationTest {

    @Test
    fun `decodes a TMDB page and ignores unknown fields`() {
        val json = """
            {
              "page": 1,
              "results": [
                {
                  "adult": false,
                  "id": 550,
                  "title": "Clube da Luta",
                  "overview": "Um homem deprimido...",
                  "poster_path": "/poster.jpg",
                  "backdrop_path": null,
                  "release_date": "1999-10-15",
                  "vote_average": 8.4,
                  "genre_ids": [18]
                }
              ],
              "total_pages": 500,
              "total_results": 10000
            }
        """.trimIndent()

        val page = NetworkJson.decodeFromString<NetworkPagedResponse<NetworkMovie>>(json)

        assertEquals(500, page.totalPages)
        val movie = page.results.single()
        assertEquals(550L, movie.id)
        assertEquals("Clube da Luta", movie.title)
        assertEquals("/poster.jpg", movie.posterPath)
        assertNull(movie.backdropPath)
        assertEquals(8.4, movie.voteAverage, 0.0)
    }

    @Test
    fun `uses defaults when optional fields are missing`() {
        val movie = NetworkJson.decodeFromString<NetworkMovie>("""{ "id": 1, "title": "Sem dados" }""")

        assertEquals("", movie.overview)
        assertNull(movie.releaseDate)
        assertEquals(0.0, movie.voteAverage, 0.0)
    }
}
