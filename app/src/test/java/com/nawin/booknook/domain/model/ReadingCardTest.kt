package com.nawin.booknook.domain.model

import com.nawin.booknook.util.book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingCardTest {

    private fun finished(id: String, date: Long) = book(id).copy(status = ReadingStatus.FINISHED, finishDate = date)

    @Test
    fun `finished and abandoned books with a date are stamped in date order`() {
        val books = listOf(
            finished("late", 300),
            book("dnf").copy(status = ReadingStatus.DNF, finishDate = 200),
            finished("early", 100),
            finished("no date", 0).copy(finishDate = null),
            book("reading").copy(status = ReadingStatus.READING, finishDate = 50),
        )

        val card = ReadingCard.current(books)

        assertEquals(1, card.number)
        assertEquals(listOf("early", "dnf", "late"), card.stamps.map { it.bookId })
        assertEquals(listOf(false, true, false), card.stamps.map { it.abandoned })
        assertEquals(ReadingCard.SLOTS - 3, card.remaining)
    }

    @Test
    fun `an empty library starts on card one`() {
        val card = ReadingCard.current(emptyList())

        assertEquals(1, card.number)
        assertTrue(card.stamps.isEmpty())
        assertFalse(card.complete)
    }

    @Test
    fun `a full card stays on show until the next stamp opens a new one`() {
        val full = (1..ReadingCard.SLOTS).map { finished("$it", it.toLong()) }

        val complete = ReadingCard.current(full)
        assertEquals(1, complete.number)
        assertTrue(complete.complete)

        val next = ReadingCard.current(full + finished("13", 13))
        assertEquals(2, next.number)
        assertEquals(listOf("13"), next.stamps.map { it.bookId })
    }
}
