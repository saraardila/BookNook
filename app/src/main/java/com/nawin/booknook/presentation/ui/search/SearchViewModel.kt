package com.nawin.booknook.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.usecase.AddBookToLibraryUseCase
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import com.nawin.booknook.domain.usecase.SearchBooksUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Search tab: queries Open Library as the user types and marks the results
 * that are already in the library.
 */
class SearchViewModel(
    private val searchBooksUseCase: SearchBooksUseCase,
    private val addBookToLibraryUseCase: AddBookToLibraryUseCase,
    getMyLibraryUseCase: GetMyLibraryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _query
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .filter { it.length >= MIN_QUERY_LENGTH }
                // collectLatest, so a slower earlier search is cancelled instead of
                // landing on top of the results the user is actually waiting for
                .collectLatest { search(it) }
        }
        viewModelScope.launch {
            getMyLibraryUseCase().collect { books ->
                _uiState.update { it.copy(library = books.associateBy(Book::id)) }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(books = emptyList(), error = null) }
        }
    }

    private suspend fun search(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        searchBooksUseCase(query)
            .onSuccess { books ->
                _uiState.update { state ->
                    // Ordered once, when the results arrive: adding a book later must
                    // not make its row jump to the top under the user's finger.
                    val ordered = books.sortedByDescending { it.id in state.library }
                    state.copy(isLoading = false, books = ordered, error = null)
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
    }

    /** Adds [book] to the library; the library flow then marks it as shelved. */
    fun addToLibrary(book: Book) {
        viewModelScope.launch { addBookToLibraryUseCase(book) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 500L
        const val MIN_QUERY_LENGTH = 2
    }
}

/**
 * @property books search results, those already in the library first.
 * @property library the user's books by id, to show each result's state.
 */
data class SearchUiState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val error: String? = null,
    val library: Map<String, Book> = emptyMap(),
)
