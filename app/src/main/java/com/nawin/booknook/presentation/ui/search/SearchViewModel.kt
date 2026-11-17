package com.nawin.booknook.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.usecase.AddBookToLibraryUseCase
import com.nawin.booknook.domain.usecase.SearchBooksUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchBooksUseCase: SearchBooksUseCase,
    private val addBookToLibraryUseCase: AddBookToLibraryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _query
                .debounce(500)
                .filter { it.length >= 2 }
                // collectLatest, so a slower earlier search is cancelled instead of
                // landing on top of the results the user is actually waiting for
                .collectLatest { search(it) }
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
                _uiState.update { it.copy(isLoading = false, books = books, error = null) }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
    }

    fun addToLibrary(book: Book) {
        viewModelScope.launch {
            addBookToLibraryUseCase(book)
            _uiState.update { state ->
                state.copy(addedBookIds = state.addedBookIds + book.id)
            }
        }
    }
}

data class SearchUiState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val error: String? = null,
    val addedBookIds: Set<String> = emptySet()
)