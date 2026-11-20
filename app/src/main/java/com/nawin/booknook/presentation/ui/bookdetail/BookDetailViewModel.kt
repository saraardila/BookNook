package com.nawin.booknook.presentation.ui.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingMood
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

class BookDetailViewModel(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val clock: Clock,
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

    /**
     * Changes the reading status. Finishing a book or putting it down records today as its
     * finish date, which is what stamps it on the reading card; starting one records the
     * start date the first time.
     */
    fun updateStatus(status: ReadingStatus) {
        val book = _uiState.value.book ?: return
        if (status == book.status) return
        viewModelScope.launch { updateBookUseCase(book.withStatus(status, clock.millis())) }
    }

    private fun Book.withStatus(status: ReadingStatus, now: Long) = copy(
        status = status,
        finishDate = if (status == ReadingStatus.FINISHED || status == ReadingStatus.DNF) now else finishDate,
        startDate = if (status == ReadingStatus.READING && startDate == null) now else startDate,
    )

    /**
     * Sets the current page, kept between 0 and the page count when it is known.
     * The new page shows at once, before the database emits it back, so quick
     * taps on the page buttons build on each other instead of on a stale book.
     * The pages moved are logged on today's date for the reading streak.
     */
    fun updateProgress(currentPage: Int) {
        val book = _uiState.value.book ?: return
        val page = currentPage.coerceIn(0, book.pageCount ?: Int.MAX_VALUE)
        if (page == book.currentPage) return
        _uiState.update { it.copy(book = book.copy(currentPage = page)) }
        val today = LocalDate.now(clock).toEpochDay()
        viewModelScope.launch { updateReadingProgressUseCase(book, page, today) }
    }

    /** Moves the current page forwards or backwards by [delta] pages. */
    fun changePage(delta: Int) {
        val book = _uiState.value.book ?: return
        updateProgress(book.currentPage + delta)
    }

    /** Files the book under [shelf], or takes it off its shelf with null. */
    fun updateShelf(shelf: ShelfTheme?) {
        val book = _uiState.value.book ?: return
        if (book.shelf == shelf) return
        viewModelScope.launch { updateBookUseCase(book.copy(shelf = shelf)) }
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

    /**
     * Saves what the reader wrote in the note sheet: the page she is on (null leaves it as
     * it is) and, when she wrote something or picked a mood, a note at that page.
     */
    fun saveNote(page: Int?, mood: ReadingMood?, content: String) {
        page?.let(::updateProgress)
        val book = _uiState.value.book ?: return
        addNoteIfAny(book.currentPage.takeIf { book.status == ReadingStatus.READING }, mood, content)
        showNoteSheet(false)
    }

    /**
     * "I finished it!" from the note sheet: moves to the last page (logged for the streak),
     * marks the book as finished and keeps the note. The writes run one after the other in
     * a single coroutine so the status change cannot be overwritten by the page change.
     */
    fun finishFromNote(mood: ReadingMood?, content: String) {
        val book = _uiState.value.book ?: return
        if (book.status == ReadingStatus.FINISHED) return
        val lastPage = book.pageCount ?: book.currentPage
        val today = LocalDate.now(clock).toEpochDay()
        val finished = book.copy(currentPage = lastPage).withStatus(ReadingStatus.FINISHED, clock.millis())
        _uiState.update { it.copy(book = finished, showNoteSheet = false) }
        viewModelScope.launch {
            updateReadingProgressUseCase(book, lastPage, today)
            updateBookUseCase(finished)
        }
        addNoteIfAny(lastPage, mood, content)
    }

    private fun addNoteIfAny(page: Int?, mood: ReadingMood?, content: String) {
        if (content.isBlank() && mood == null) return
        val note = BookNote(
            bookId = bookId,
            chapter = null,
            content = content.trim(),
            page = page,
            mood = mood,
            createdAt = clock.millis(),
        )
        viewModelScope.launch { addNoteUseCase(note) }
    }

    fun showNoteSheet(show: Boolean) {
        _uiState.update { it.copy(showNoteSheet = show) }
    }
    fun updateStartDate(date: Long) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            updateBookUseCase(book.copy(startDate = date))
        }
    }

    fun updateFinishDate(date: Long) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            updateBookUseCase(book.copy(finishDate = date))
        }
    }
}

data class BookDetailUiState(
    val book: Book? = null,
    val notes: List<BookNote> = emptyList(),
    val showNoteSheet: Boolean = false,
)