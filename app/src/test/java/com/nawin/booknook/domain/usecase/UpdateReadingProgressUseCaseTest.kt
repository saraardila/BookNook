package com.nawin.booknook.domain.usecase

import com.nawin.booknook.util.FakeBookRepository
import com.nawin.booknook.util.FakeReadingLogRepository
import com.nawin.booknook.util.book
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateReadingProgressUseCaseTest {

    private val books = FakeBookRepository()
    private val readingLog = FakeReadingLogRepository()
    private val updateProgress = UpdateReadingProgressUseCase(books, readingLog)
    private val today = 20_000L

    @Test
    fun `moving forward saves the page and logs the pages read today`() = runTest {
        val throne = book("throne").copy(currentPage = 40)

        updateProgress(throne, page = 52, today = today)

        assertEquals(52, books.updatedBooks.single().currentPage)
        assertEquals(mapOf(today to 12), readingLog.pagesPerDay().first())
    }

    @Test
    fun `going back corrects today's pages without going below zero`() = runTest {
        val throne = book("throne").copy(currentPage = 40)

        updateProgress(throne, page = 60, today = today)
        updateProgress(throne.copy(currentPage = 60), page = 50, today = today)
        updateProgress(throne.copy(currentPage = 50), page = 10, today = today)

        assertEquals(mapOf(today to 0), readingLog.pagesPerDay().first())
    }

    @Test
    fun `the same page changes nothing`() = runTest {
        updateProgress(book("throne").copy(currentPage = 40), page = 40, today = today)

        assertTrue(books.updatedBooks.isEmpty())
        assertTrue(readingLog.pagesPerDay().first().isEmpty())
    }
}
