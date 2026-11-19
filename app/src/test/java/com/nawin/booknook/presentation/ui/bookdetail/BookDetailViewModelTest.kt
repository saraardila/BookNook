package com.nawin.booknook.presentation.ui.bookdetail

import androidx.lifecycle.SavedStateHandle
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.AddNoteUseCase
import com.nawin.booknook.domain.usecase.GetBookByIdUseCase
import com.nawin.booknook.domain.usecase.GetNotesUseCase
import com.nawin.booknook.domain.usecase.UpdateBookUseCase
import com.nawin.booknook.util.FakeBookRepository
import com.nawin.booknook.util.MainDispatcherRule
import com.nawin.booknook.util.book
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BookDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookRepository().apply {
        library.value = listOf(
            book(BOOK_ID).copy(status = ReadingStatus.READING, pageCount = PAGE_COUNT, currentPage = START_PAGE),
        )
    }

    // Lazy so it is built after the rule has replaced Dispatchers.Main.
    private val viewModel by lazy {
        BookDetailViewModel(
            GetBookByIdUseCase(repository),
            UpdateBookUseCase(repository),
            GetNotesUseCase(repository),
            AddNoteUseCase(repository),
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

    private companion object {
        const val BOOK_ID = "OL1W"
        const val PAGE_COUNT = 404
        const val START_PAGE = 212
    }
}
