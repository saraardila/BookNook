package com.nawin.booknook.presentation.ui.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getBookByIdUseCase(bookId).collect { book ->
                _uiState.update { it.copy(book = book) }
            }
        }
        viewModelScope.launch {
            getNotesUseCase(bookId).collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
    }

    fun updateRating(rating: Float) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            updateBookUseCase(book.copy(rating = rating))
        }
    }

    fun updateStatus(status: ReadingStatus) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            val updated = book.copy(
                status = status,
                finishDate = if (status == ReadingStatus.FINISHED)
                    System.currentTimeMillis() else book.finishDate,
                startDate = if (status == ReadingStatus.READING && book.startDate == null)
                    System.currentTimeMillis() else book.startDate
            )
            updateBookUseCase(updated)
        }
    }

    fun updateProgress(currentPage: Int) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            updateBookUseCase(book.copy(currentPage = currentPage))
        }
    }

    fun toggleFavorite() {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            updateBookUseCase(book.copy(isFavorite = !book.isFavorite))
        }
    }

    fun updateReview(review: String) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            updateBookUseCase(book.copy(review = review))
        }
    }

    fun addNote(content: String, chapter: String?) {
        if (content.isBlank()) return
        viewModelScope.launch {
            addNoteUseCase(
                BookNote(
                    bookId = bookId,
                    content = content,
                    chapter = chapter.takeIf { !it.isNullOrBlank() },
                    page = _uiState.value.book?.currentPage
                )
            )
        }
    }

    fun showProgressDialog(show: Boolean) {
        _uiState.update { it.copy(showProgressDialog = show) }
    }

    fun showNoteDialog(show: Boolean) {
        _uiState.update { it.copy(showNoteDialog = show) }
    }
}

data class BookDetailUiState(
    val book: Book? = null,
    val notes: List<BookNote> = emptyList(),
    val showProgressDialog: Boolean = false,
    val showNoteDialog: Boolean = false
)