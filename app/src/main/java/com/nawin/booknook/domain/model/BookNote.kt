package com.nawin.booknook.domain.model

/**
 * A diary entry: a note on a book, or a loose thought when [bookId] is null.
 *
 * @property page Page the reader was on when writing it, if any.
 * @property mood How the reading left her, picked from a fixed set.
 */
data class BookNote(
    val id: Long = 0,
    val bookId: String?,
    val chapter: String?,
    val content: String,
    val page: Int?,
    val mood: ReadingMood? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
