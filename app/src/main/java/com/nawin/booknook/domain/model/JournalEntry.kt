package com.nawin.booknook.domain.model

/**
 * One page of the reading diary. Every entry has a [date] (epoch millis), so the
 * diary can be split by month; books without a finish date are left out.
 */
sealed interface JournalEntry {
    val date: Long

    /** Stable key for lists. */
    val key: String

    /** A finished book, with its rating and review when she left them. */
    data class Finished(val book: Book, override val date: Long) : JournalEntry {
        override val key get() = "finished-${book.id}"
        val hasReview get() = !book.review.isNullOrBlank() || book.rating > 0f
    }

    /** A note on a book, or a loose thought when [book] is null. */
    data class Note(val note: BookNote, val book: Book?) : JournalEntry {
        override val date get() = note.createdAt
        override val key get() = "note-${note.id}"
    }

    /** A book she put down, and the page she left it on. */
    data class PutDown(val book: Book, override val date: Long) : JournalEntry {
        override val key get() = "put-down-${book.id}"
    }

    companion object {
        /** Every entry from the library and the notes, newest first. */
        fun from(books: List<Book>, notes: List<BookNote>): List<JournalEntry> {
            val byId = books.associateBy { it.id }
            val endings = books.mapNotNull { book ->
                val finished = book.finishDate ?: return@mapNotNull null
                when (book.status) {
                    ReadingStatus.FINISHED -> Finished(book, finished)
                    ReadingStatus.DNF -> PutDown(book, finished)
                    else -> null
                }
            }
            val written = notes.map { Note(it, it.bookId?.let(byId::get)) }
            return (endings + written).sortedByDescending { it.date }
        }
    }
}
