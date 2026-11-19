package com.nawin.booknook.presentation.components.library

import org.junit.Assert.assertEquals
import org.junit.Test

class SpineCodeTest {

    @Test
    fun `uses the first three letters of the surname and the title initial`() {
        assertEquals(SpineCode("MAA", "t"), SpineCode.from("Sarah J. Maas", "Throne of Glass"))
    }

    @Test
    fun `skips a leading article in English and Spanish`() {
        assertEquals("a", SpineCode.from("Sarah J. Maas", "The Assassin's Blade").title)
        assertEquals("c", SpineCode.from("Sarah J. Maas", "Una corte de rosas y espinas").title)
    }

    @Test
    fun `keeps the article when the title is only an article`() {
        assertEquals("u", SpineCode.from("Someone", "Una").title)
    }

    @Test
    fun `removes accents and punctuation`() {
        assertEquals(SpineCode("GAR", "c"), SpineCode.from("Gabriel García", "¿Cien años de soledad?"))
    }

    @Test
    fun `short surnames and empty input give short or empty codes`() {
        assertEquals("LI", SpineCode.from("Ken Li", "Book").author)
        assertEquals(SpineCode("", ""), SpineCode.from("  ", ""))
    }
}
