package com.nawin.booknook.domain.model

import com.nawin.booknook.util.book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShelfLicenceTest {

    private fun finished(id: String, shelf: ShelfTheme?) =
        book(id).copy(status = ReadingStatus.FINISHED, shelf = shelf)

    @Test
    fun `every genre shelf is listed, without the standard theme`() {
        val licences = ShelfLicence.fromLibrary(emptyList())

        assertEquals(ShelfLicence.genreShelves, licences.map { it.shelf })
        assertFalse(ShelfTheme.STANDARD in licences.map { it.shelf })
        assertTrue(licences.all { it.booksRead == 0 && !it.earned })
    }

    @Test
    fun `only finished books on a shelf count`() {
        val books = listOf(
            finished("a", ShelfTheme.WITCHY),
            finished("b", ShelfTheme.WITCHY),
            finished("c", null),
            book("d").copy(status = ReadingStatus.DNF, shelf = ShelfTheme.WITCHY),
            book("e").copy(status = ReadingStatus.READING, shelf = ShelfTheme.WITCHY),
        )

        val witchy = ShelfLicence.fromLibrary(books).single { it.shelf == ShelfTheme.WITCHY }

        assertEquals(2, witchy.booksRead)
        assertFalse(witchy.earned)
    }

    @Test
    fun `five finished books earn the licence and progress stops at five`() {
        val books = (1..7).map { finished("$it", ShelfTheme.ROMANTASY) }

        val romantasy = ShelfLicence.fromLibrary(books).single { it.shelf == ShelfTheme.ROMANTASY }

        assertTrue(romantasy.earned)
        assertEquals(7, romantasy.booksRead)
        assertEquals(ShelfLicence.BOOKS_PER_LICENCE, romantasy.progress)
    }
}
