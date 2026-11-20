package com.nawin.booknook.domain.model

import com.nawin.booknook.util.book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JournalEntryTest {

    @Test
    fun `finished and put-down books with a date become entries, others do not`() {
        val books = listOf(
            book("finished").copy(status = ReadingStatus.FINISHED, finishDate = 30),
            book("dnf").copy(status = ReadingStatus.DNF, finishDate = 20),
            book("no date").copy(status = ReadingStatus.FINISHED, finishDate = null),
            book("reading").copy(status = ReadingStatus.READING, finishDate = 10),
        )

        val entries = JournalEntry.from(books, emptyList())

        assertEquals(listOf("finished-finished", "put-down-dnf"), entries.map { it.key })
    }

    @Test
    fun `notes keep their book, loose thoughts have none, and all are sorted newest first`() {
        val fourthWing = book("fw")
        val notes = listOf(
            BookNote(id = 1, bookId = "fw", chapter = null, content = "Xaden", page = 100, createdAt = 5),
            BookNote(id = 2, bookId = null, chapter = null, content = "Hay libros que se viven", page = null, createdAt = 50),
        )
        val finished = fourthWing.copy(status = ReadingStatus.FINISHED, finishDate = 20)

        val entries = JournalEntry.from(listOf(finished), notes)

        assertEquals(listOf(50L, 20L, 5L), entries.map { it.date })
        assertNull((entries[0] as JournalEntry.Note).book)
        assertEquals("fw", (entries[2] as JournalEntry.Note).book?.id)
    }

    @Test
    fun `a finished book counts as reviewed with a rating or a review`() {
        val base = book("b").copy(status = ReadingStatus.FINISHED, finishDate = 1)
        assertEquals(false, JournalEntry.Finished(base, 1).hasReview)
        assertEquals(true, JournalEntry.Finished(base.copy(rating = 3f), 1).hasReview)
        assertEquals(true, JournalEntry.Finished(base.copy(review = "¡YA!"), 1).hasReview)
    }
}
