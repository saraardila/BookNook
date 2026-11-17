package com.nawin.booknook.presentation.ui.search

import com.nawin.booknook.domain.usecase.AddBookToLibraryUseCase
import com.nawin.booknook.domain.usecase.SearchBooksUseCase
import com.nawin.booknook.util.FakeBookRepository
import com.nawin.booknook.util.MainDispatcherRule
import com.nawin.booknook.util.book
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookRepository()

    private fun viewModel() = SearchViewModel(
        SearchBooksUseCase(repository),
        AddBookToLibraryUseCase(repository)
    )

    /**
     * What happened on the device: Open Library answered 422 for the short query
     * "he" after the full query had already returned its results.
     */
    @Test
    fun `a slow failing search does not leave its error on screen`() = runTest {
        repository.onSearch = { query ->
            if (query == "he") {
                delay(3_000)
                Result.failure(IOException("Couldn't connect"))
            } else {
                delay(100)
                Result.success(listOf(book("OL1W", "Heir of Fire")))
            }
        }
        val viewModel = viewModel()

        viewModel.onQueryChange("he")
        advanceTimeBy(600) // past the debounce, so the slow search starts
        viewModel.onQueryChange("heir of fire")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(listOf("OL1W"), viewModel.uiState.value.books.map { it.id })
    }

    @Test
    fun `a slow search does not overwrite the results of a newer one`() = runTest {
        repository.onSearch = { query ->
            if (query == "he") {
                delay(3_000)
                Result.success(listOf(book("STALE")))
            } else {
                delay(100)
                Result.success(listOf(book("FRESH")))
            }
        }
        val viewModel = viewModel()

        viewModel.onQueryChange("he")
        advanceTimeBy(600)
        viewModel.onQueryChange("heir of fire")
        advanceUntilIdle()

        assertEquals(listOf("FRESH"), viewModel.uiState.value.books.map { it.id })
    }

    @Test
    fun `clearing the query clears results and errors`() = runTest {
        repository.onSearch = { Result.failure(IOException("Couldn't connect")) }
        val viewModel = viewModel()

        viewModel.onQueryChange("heir")
        advanceUntilIdle()
        viewModel.onQueryChange("")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(emptyList<String>(), viewModel.uiState.value.books.map { it.id })
    }
}
