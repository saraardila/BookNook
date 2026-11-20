package com.nawin.booknook.domain.model

/**
 * A reader's licence for one shelf: earned by finishing [BOOKS_PER_LICENCE] books filed under it.
 *
 * @property booksRead Finished books on this shelf; can go past [BOOKS_PER_LICENCE].
 */
data class ShelfLicence(
    val shelf: ShelfTheme,
    val booksRead: Int,
) {
    val earned: Boolean get() = booksRead >= BOOKS_PER_LICENCE

    /** Books towards the licence, capped at [BOOKS_PER_LICENCE] for "3/5"-style progress. */
    val progress: Int get() = minOf(booksRead, BOOKS_PER_LICENCE)

    companion object {
        const val BOOKS_PER_LICENCE = 5

        /** Shelves a book can be filed under. The standard theme is a look, not a genre. */
        val genreShelves: List<ShelfTheme> = ShelfTheme.entries - ShelfTheme.STANDARD

        /**
         * One licence per genre shelf, in [genreShelves] order. Only finished books count:
         * an abandoned one does not earn a licence.
         */
        fun fromLibrary(books: List<Book>): List<ShelfLicence> {
            val finished = books.filter { it.status == ReadingStatus.FINISHED }.groupingBy { it.shelf }.eachCount()
            return genreShelves.map { ShelfLicence(it, finished[it] ?: 0) }
        }
    }
}
