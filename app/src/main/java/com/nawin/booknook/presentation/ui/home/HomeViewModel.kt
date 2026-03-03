package com.nawin.booknook.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMyLibraryUseCase: GetMyLibraryUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        getMyLibraryUseCase(ReadingStatus.READING),
        getMyLibraryUseCase(ReadingStatus.FINISHED),
        getMyLibraryUseCase()
    ) { reading, finished, all ->
        HomeUiState(
            currentlyReading = reading.firstOrNull(),
            recentlyFinished = finished.take(4),
            totalBooks = all.size,
            booksFinished = finished.size,
            booksReading = reading.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}

data class HomeUiState(
    val currentlyReading: Book? = null,
    val recentlyFinished: List<Book> = emptyList(),
    val totalBooks: Int = 0,
    val booksFinished: Int = 0,
    val booksReading: Int = 0
)