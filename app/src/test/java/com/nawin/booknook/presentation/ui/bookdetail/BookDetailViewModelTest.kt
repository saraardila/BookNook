package com.nawin.booknook.presentation.ui.bookdetail

import androidx.lifecycle.SavedStateHandle
import com.nawin.booknook.domain.model.ReadingMood
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.AddNoteUseCase
import com.nawin.booknook.domain.usecase.GetBookByIdUseCase
import com.nawin.booknook.domain.usecase.GetNotesUseCase
import com.nawin.booknook.domain.usecase.UpdateBookUseCase
import com.nawin.booknook.domain.usecase.UpdateReadingProgressUseCase
import com.nawin.booknook.util.FakeBookRepository
import com.nawin.booknook.util.FakeReadingLogRepository
import com.nawin.booknook.util.MainDispatcherRule
import com.nawin.booknook.util.book
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BookDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookRepository().apply {
        library.value = listOf(
            book(BOOK_ID).copy(status = ReadingStatus.READING, pageCount = PAGE_COUNT, currentPage = START_PAGE),
        )
    }

    private val readingLog = FakeReadingLogRepository()
    private val today = LocalDate.of(2026, 11, 20)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    // Lazy so it is built after the rule has replaced Dispatchers.Main.
    private val viewModel by lazy {
        BookDetailViewModel(
            GetBookByIdUseCase(repository),
            UpdateBookUseCase(repository),
            GetNotesUseCase(repository),
            AddNoteUseCase(repository),
            UpdateReadingProgressUseCase(repository, readingLog),
            clock,
            SavedStateHandle(mapOf("bookId" to BOOK_ID)),
        )
    }

    private val currentPage get() = viewModel.uiState.value.book?.currentPage

    /** Two taps before the database answers must count as two pages, not one. */
    @Test
    fun `quick page steps build on each other`() = runTest {
        viewModel
        advanceUntilIdle()

        viewModel.changePage(1)
        viewModel.changePage(1)
        advanceUntilIdle()

        assertEquals(START_PAGE + 2, currentPage)
        assertEquals(START_PAGE + 2, repository.library.value.single().currentPage)
    }

    @Test
    fun `pages moved forward count for today's streak`() = runTest {
        viewModel
        advanceUntilIdle()

        viewModel.changePage(3)
        viewModel.changePage(4)
        advanceUntilIdle()

        assertEquals(mapOf(today.toEpochDay() to 7), readingLog.pagesPerDay().first())
    }

    @Test
    fun `page stays between zero and the page count`() = runTest {
        viewModel
        advanceUntilIdle()

        viewModel.updateProgress(PAGE_COUNT + 50)
        advanceUntilIdle()
        assertEquals(PAGE_COUNT, currentPage)

        viewModel.updateProgress(-3)
        advanceUntilIdle()
        assertEquals(0, currentPage)
    }

    @Test
    fun `a step that changes nothing is not saved`() = runTest {
        viewModel
        advanceUntilIdle()
        viewModel.updateProgress(0)
        advanceUntilIdle()
        repository.updatedBooks.clear()

        viewModel.changePage(-1)
        advanceUntilIdle()

        assertTrue(repository.updatedBooks.isEmpty())
    }

    /** Both end a reading and so both earn a stamp on the reading card, which needs a date. */
    @Test
    fun `finishing or putting a book down records today as its finish date`() = runTest {
        viewModel
        advanceUntilIdle()

        viewModel.updateStatus(ReadingStatus.DNF)
        advanceUntilIdle()

        val stored = repository.library.value.single()
        assertEquals(ReadingStatus.DNF, stored.status)
        assertEquals(clock.millis(), stored.finishDate)
    }

    @Test
    fun `saving the note sheet moves the page and keeps the note at it`() = runTest {
        viewModel
        advanceUntilIdle()

        viewModel.saveNote(START_PAGE + 10, ReadingMood.HOOKED, "  Dorian, no  ")
        advanceUntilIdle()

        assertEquals(START_PAGE + 10, repository.library.value.single().currentPage)
        val note = repository.addedNotes.single()
        assertEquals(BOOK_ID, note.bookId)
        assertEquals(START_PAGE + 10, note.page)
        assertEquals(ReadingMood.HOOKED, note.mood)
        assertEquals("Dorian, no", note.content)
        assertEquals(clock.millis(), note.createdAt)
    }

    @Test
    fun `a note without text or mood is not saved`() = runTest {
        viewModel
        advanceUntilIdle()

        viewModel.saveNote(START_PAGE, null, "   ")
        advanceUntilIdle()

        assertTrue(repository.addedNotes.isEmpty())
    }

    /** The last pages count for the streak, and the page change must not undo the status. */
    @Test
    fun `finishing from the note sheet jumps to the last page and marks it finished`() = runTest {
        viewModel
        advanceUntilIdle()

        viewModel.finishFromNote(ReadingMood.IN_TEARS, "")
        advanceUntilIdle()

        val stored = repository.library.value.single()
        assertEquals(ReadingStatus.FINISHED, stored.status)
        assertEquals(PAGE_COUNT, stored.currentPage)
        assertEquals(clock.millis(), stored.finishDate)
        assertEquals(mapOf(today.toEpochDay() to PAGE_COUNT - START_PAGE), readingLog.pagesPerDay().first())
        assertEquals(PAGE_COUNT, repository.addedNotes.single().page)
    }

    private companion object {
        const val BOOK_ID = "OL1W"
        const val PAGE_COUNT = 404
        const val START_PAGE = 212
    }
}
