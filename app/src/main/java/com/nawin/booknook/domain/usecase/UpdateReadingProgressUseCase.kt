package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.repository.BookRepository
import com.nawin.booknook.domain.repository.ReadingLogRepository

/**
 * Moves [Book.currentPage] and logs the difference as pages read on that day,
 * which is what the reading streak is built from.
 */
class UpdateReadingProgressUseCase(
    private val books: BookRepository,
    private val readingLog: ReadingLogRepository,
) {
    /**
     * @param book The book as it was before this change.
     * @param page The new current page, already validated by the caller.
     * @param today Epoch day the pages are logged on.
     */
    suspend operator fun invoke(book: Book, page: Int, today: Long) {
        val pagesRead = page - book.currentPage
        if (pagesRead == 0) return
        books.updateBook(book.copy(currentPage = page))
        readingLog.logPages(today, book.id, pagesRead)
    }
}
