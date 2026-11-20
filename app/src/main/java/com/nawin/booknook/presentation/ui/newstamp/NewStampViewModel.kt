package com.nawin.booknook.presentation.ui.newstamp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingCard
import com.nawin.booknook.domain.model.ShelfLicence
import com.nawin.booknook.domain.usecase.GetBookByIdUseCase
import com.nawin.booknook.domain.usecase.GetReadingCardUseCase
import com.nawin.booknook.domain.usecase.GetShelfLicencesUseCase
import com.nawin.booknook.domain.usecase.UpdateBookUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Feeds the "new stamp" screen shown right after a book is marked as finished. */
class NewStampViewModel(
    getBookByIdUseCase: GetBookByIdUseCase,
    getReadingCardUseCase: GetReadingCardUseCase,
    getShelfLicencesUseCase: GetShelfLicencesUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle[BOOK_ID_ARG])

    val uiState: StateFlow<NewStampUiState> = combine(
        getBookByIdUseCase(bookId),
        getReadingCardUseCase(),
        getShelfLicencesUseCase(),
    ) { book, card, licences ->
        NewStampUiState(
            book = book,
            card = card,
            licence = book?.shelf?.let { shelf -> licences.firstOrNull { it.shelf == shelf } },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NewStampUiState())

    fun updateRating(rating: Float) {
        val book = uiState.value.book ?: return
        viewModelScope.launch { updateBookUseCase(book.copy(rating = rating)) }
    }

    companion object {
        const val BOOK_ID_ARG = "bookId"
    }
}

/**
 * @property licence Progress on the book's shelf licence, or null when it has no shelf yet.
 */
data class NewStampUiState(
    val book: Book? = null,
    val card: ReadingCard? = null,
    val licence: ShelfLicence? = null,
)
