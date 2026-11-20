package com.nawin.booknook.domain.model

/**
 * A line the reader wrote, shown on the home screen for one day.
 *
 * @property bookTitle The book the note belongs to, or null for a loose thought.
 */
data class DailyQuote(
    val text: String,
    val bookTitle: String?,
) {
    companion object {
        /**
         * Picks one of the notes with text for [epochDay]: the same one all day
         * and the next one the following day, cycling through them oldest first.
         * Null when there are none.
         */
        fun pick(notes: List<BookNote>, books: List<Book>, epochDay: Long): DailyQuote? {
            val written = notes.filter { it.content.isNotBlank() }.sortedWith(compareBy({ it.createdAt }, { it.id }))
            if (written.isEmpty()) return null
            val note = written[epochDay.mod(written.size.toLong()).toInt()]
            return DailyQuote(
                text = note.content.trim(),
                bookTitle = note.bookId?.let { id -> books.firstOrNull { it.id == id }?.title },
            )
        }
    }
}
