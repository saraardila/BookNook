package com.nawin.booknook.domain.model

/**
 * One stamp on the reading card: a book the reader finished or gave up on, on the day it happened.
 *
 * @property date Finish (or give-up) date in epoch milliseconds.
 * @property abandoned true for a book put down unfinished, stamped crossed out.
 */
data class CardStamp(
    val bookId: String,
    val title: String,
    val date: Long,
    val abandoned: Boolean,
)

/**
 * A reading card with [SLOTS] stamp spaces, filled in date order. Cards are numbered from 1:
 * once one is full, the next stamp opens a new card.
 */
data class ReadingCard(
    val number: Int,
    val stamps: List<CardStamp>,
) {
    val complete: Boolean get() = stamps.size >= SLOTS
    val remaining: Int get() = SLOTS - stamps.size

    companion object {
        const val SLOTS = 12

        /**
         * The card being filled: a full one stays on show until the next stamp starts a new card.
         * Only books with a finish date can be stamped; ties keep the library order.
         */
        fun current(books: List<Book>): ReadingCard {
            val stamps = stampsOf(books)
            val number = if (stamps.isEmpty()) 1 else (stamps.size - 1) / SLOTS + 1
            return ReadingCard(number, stamps.drop((number - 1) * SLOTS))
        }

        /** Every stamp the reader has earned, oldest first. */
        fun stampsOf(books: List<Book>): List<CardStamp> = books
            .filter { (it.status == ReadingStatus.FINISHED || it.status == ReadingStatus.DNF) && it.finishDate != null }
            .sortedBy { it.finishDate }
            .map { CardStamp(it.id, it.title, it.finishDate!!, abandoned = it.status == ReadingStatus.DNF) }
    }
}
