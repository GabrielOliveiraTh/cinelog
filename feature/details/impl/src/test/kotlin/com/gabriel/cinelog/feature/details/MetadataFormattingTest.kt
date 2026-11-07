package com.gabriel.cinelog.feature.details

import java.util.Locale
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class MetadataFormattingTest {

    @Test
    fun `formats runtime in hours and minutes`() {
        assertEquals("2h 32min", formatRuntime(152))
        assertEquals("2h", formatRuntime(120))
        assertEquals("45min", formatRuntime(45))
    }

    @Test
    fun `metadata line skips unknown values`() {
        assertEquals("2008 · 2h 32min · ★ 8.5", metadataLine(LocalDate(2008, 7, 16), 152, 8.5, Locale.US))
        assertEquals("★ 7.0", metadataLine(null, null, 7.0, Locale.US))
    }

    @Test
    fun `rating follows the locale decimal separator`() {
        assertEquals("★ 8,5", metadataLine(null, null, 8.5, Locale.forLanguageTag("pt-BR")))
    }
}
