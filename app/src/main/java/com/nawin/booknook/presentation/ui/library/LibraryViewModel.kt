package com.nawin.booknook.presentation.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Shelves tab: the whole library, filtered by reading status or favourites. */
class LibraryViewModel(
    getMyLibraryUseCase: GetMyLibraryUseCase,
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(LibraryFilter.ALL)
    val selectedFilter: StateFlow<LibraryFilter> = _selectedFilter.asStateFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        getMyLibraryUseCase(),
        _selectedFilter,
    ) { books, filter ->
        val filtered = books.filter(filter::matches)
        LibraryUiState(
            books = filtered,
            totalBooks = books.size,
            counts = LibraryFilter.entries.associateWith { f -> books.count(f::matches) },
            isEmpty = filtered.isEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState(),
    )

    fun setFilter(filter: LibraryFilter) {
        _selectedFilter.value = filter
    }
}

/** Filters shown as chips on the shelves screen. Labels live in the UI layer. */
enum class LibraryFilter {
    ALL,
    READING,
    FINISHED,
    WANT_TO_READ,
    DNF,
    FAVORITES;

    fun matches(book: Book): Boolean = when (this) {
        ALL -> true
        READING -> book.status == ReadingStatus.READING
        FINISHED -> book.status == ReadingStatus.FINISHED
        WANT_TO_READ -> book.status == ReadingStatus.WANT_TO_READ
        DNF -> book.status == ReadingStatus.DNF
        FAVORITES -> book.isFavorite
    }
}

/**
 * @property books books matching the selected filter.
 * @property totalBooks size of the whole library.
 * @property counts number of books each filter would show.
 */
data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val totalBooks: Int = 0,
    val counts: Map<LibraryFilter, Int> = emptyMap(),
    val isEmpty: Boolean = false,
)
