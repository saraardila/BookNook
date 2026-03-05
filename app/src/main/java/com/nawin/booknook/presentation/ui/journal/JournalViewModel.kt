package com.nawin.booknook.presentation.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val getMyLibraryUseCase: GetMyLibraryUseCase
) : ViewModel() {

    val uiState: StateFlow<JournalUiState> = getMyLibraryUseCase()
        .map { books ->
            JournalUiState(
                books = books
                    .filter { it.status == ReadingStatus.FINISHED }
                    .sortedByDescending { it.finishDate }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = JournalUiState()
        )
}

data class JournalUiState(
    val books: List<Book> = emptyList()
)