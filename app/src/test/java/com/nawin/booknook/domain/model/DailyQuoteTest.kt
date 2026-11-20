package com.nawin.booknook.domain.model

import com.nawin.booknook.util.book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyQuoteTest {

    private val notes = listOf(
        BookNote(2, "fw", null, "segunda", null, null, 200),
        BookNote(1, null, null, "primera", null, null, 100),
        BookNote(3, "fw", null, "  ", null, null, 300),
    )

    @Test
    fun `one note a day, cycling oldest first and skipping empty ones`() {
        val books = listOf(book("fw", title = "Fourth Wing"))

        assertEquals(DailyQuote("primera", null), DailyQuote.pick(notes, books, epochDay = 10))
        assertEquals(DailyQuote("segunda", "Fourth Wing"), DailyQuote.pick(notes, books, epochDay = 11))
        assertEquals(DailyQuote("primera", null), DailyQuote.pick(notes, books, epochDay = 12))
    }

    @Test
    fun `no notes with text means no quote`() {
        assertNull(DailyQuote.pick(notes.takeLast(1), emptyList(), epochDay = 1))
    }
}
