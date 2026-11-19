package com.nawin.booknook.presentation.ui.library

import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import com.nawin.booknook.util.FakeBookRepository
import com.nawin.booknook.util.MainDispatcherRule
import com.nawin.booknook.util.book
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookRepository().apply {
        library.value = listOf(
            book("reading").copy(status = ReadingStatus.READING),
            book("finished").copy(status = ReadingStatus.FINISHED, isFavorite = true),
            book("finished2").copy(status = ReadingStatus.FINISHED),
            book("dnf").copy(status = ReadingStatus.DNF),
        )
    }

    // Lazy so it is built after the rule has replaced Dispatchers.Main.
    private val viewModel by lazy { LibraryViewModel(GetMyLibraryUseCase(repository)) }

    /** Keeps uiState subscribed (it only runs WhileSubscribed), collecting eagerly. */
    private fun TestScope.collectState() {
        val state = viewModel.uiState
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { state.collect() }
    }

    @Test
    fun `counts every filter over the whole library`() = runTest {
        collectState()
        advanceUntilIdle()

        val counts = viewModel.uiState.value.counts
        assertEquals(4, counts[LibraryFilter.ALL])
        assertEquals(1, counts[LibraryFilter.READING])
        assertEquals(2, counts[LibraryFilter.FINISHED])
        assertEquals(0, counts[LibraryFilter.WANT_TO_READ])
        assertEquals(1, counts[LibraryFilter.DNF])
        assertEquals(1, counts[LibraryFilter.FAVORITES])
    }

    @Test
    fun `a filter narrows the books but keeps the total`() = runTest {
        collectState()
        viewModel.setFilter(LibraryFilter.FINISHED)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("finished", "finished2"), state.books.map { it.id })
        assertEquals(4, state.totalBooks)
    }

    @Test
    fun `a filter with no books is empty`() = runTest {
        collectState()
        viewModel.setFilter(LibraryFilter.WANT_TO_READ)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
    }
}
