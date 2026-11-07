package com.gabriel.cinelog.core.data.model

import com.gabriel.cinelog.core.database.model.asExternalModel
import com.gabriel.cinelog.core.network.model.NetworkCastMember
import com.gabriel.cinelog.core.network.model.NetworkCredits
import com.gabriel.cinelog.core.network.model.NetworkGenre
import com.gabriel.cinelog.core.network.model.NetworkMovie
import com.gabriel.cinelog.core.network.model.NetworkMovieDetails
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMappersTest {

    @Test
    fun `network to model keeps fields and parses the release date`() {
        val movie = NetworkMovie(id = 550, title = "Clube da Luta", releaseDate = "1999-10-15", voteAverage = 8.4)
            .asEntity()
            .asExternalModel()

        assertEquals(550L, movie.id)
        assertEquals(LocalDate(1999, 10, 15), movie.releaseDate)
        assertEquals(8.4, movie.voteAverage, 0.0)
    }

    @Test
    fun `details keep the main cast in billing order and drop empty values`() {
        val details = NetworkMovieDetails(
            id = 155,
            title = "Batman",
            tagline = "",
            runtime = 0,
            genres = listOf(NetworkGenre(28, "Ação")),
            credits = NetworkCredits(
                cast = List(30) { index ->
                    NetworkCastMember(
                        id = index.toLong(),
                        name = "Ator $index",
                        // Reversed: the last element is the lead (order 0)
                        order = 29 - index,
                    )
                },
            ),
        )

        val entity = details.asDetailsEntity(updatedAt = 0)
        val cast = details.asCastEntities()

        assertNull(entity.tagline)
        assertNull(entity.runtimeMinutes)
        assertEquals(listOf("Ação"), entity.genres)
        assertEquals(MAX_CAST_MEMBERS, cast.size)
        assertEquals("Ator 29", cast.first().name)
        assertEquals((0 until MAX_CAST_MEMBERS).toList(), cast.map { it.billingOrder })
    }

    @Test
    fun `blank or invalid release dates become null`() {
        assertNull(NetworkMovie(id = 1, title = "A", releaseDate = "").asEntity().releaseDate)
        assertNull(
            NetworkMovie(id = 2, title = "B", releaseDate = "15/10/1999").asEntity().asExternalModel().releaseDate,
        )
    }
}
