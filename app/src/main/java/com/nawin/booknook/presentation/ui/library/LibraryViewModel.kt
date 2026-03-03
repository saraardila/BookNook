package com.nawin.booknook.presentation.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getMyLibraryUseCase: GetMyLibraryUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(LibraryFilter.ALL)
    val selectedFilter: StateFlow<LibraryFilter> = _selectedFilter.asStateFlow()

    private val _allBooks = getMyLibraryUseCase()

    val uiState: StateFlow<LibraryUiState> = combine(
        _allBooks,
        _selectedFilter
    ) { books, filter ->
        val filtered = when (filter) {
            LibraryFilter.ALL          -> books
            LibraryFilter.READING      -> books.filter { it.status == ReadingStatus.READING }
            LibraryFilter.FINISHED     -> books.filter { it.status == ReadingStatus.FINISHED }
            LibraryFilter.WANT_TO_READ -> books.filter { it.status == ReadingStatus.WANT_TO_READ }
            LibraryFilter.DNF          -> books.filter { it.status == ReadingStatus.DNF }
            LibraryFilter.FAVORITES    -> books.filter { it.isFavorite }
        }
        LibraryUiState(
            books = filtered,
            totalBooks = books.size,
            isEmpty = filtered.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun setFilter(filter: LibraryFilter) {
        _selectedFilter.value = filter
    }
}

enum class LibraryFilter(val label: String, val emoji: String) {
    ALL("All", "📚"),
    READING("Reading", "📖"),
    FINISHED("Finished", "✅"),
    WANT_TO_READ("Want to Read", "🔖"),
    DNF("DNF", "🚫"),
    FAVORITES("Favourites", "❤️")
}

data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val totalBooks: Int = 0,
    val isEmpty: Boolean = false
)