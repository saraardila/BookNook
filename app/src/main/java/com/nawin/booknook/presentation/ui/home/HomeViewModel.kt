package com.nawin.booknook.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.model.ReadingStreak
import com.nawin.booknook.domain.usecase.GetReadingStreakUseCase
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import kotlinx.coroutines.flow.*
import java.time.Clock
import java.time.LocalDate

class HomeViewModel(
    private val getMyLibraryUseCase: GetMyLibraryUseCase,
    getReadingStreakUseCase: GetReadingStreakUseCase,
    clock: Clock,
) : ViewModel() {

    // Read when Home is created; a new day shows the next time the screen is opened.
    private val today = LocalDate.now(clock)

    val uiState: StateFlow<HomeUiState> = combine(
        getMyLibraryUseCase(ReadingStatus.READING),
        getMyLibraryUseCase(ReadingStatus.FINISHED),
        getMyLibraryUseCase(),
        getReadingStreakUseCase(today.toEpochDay()),
    ) { reading, finished, all, streak ->
        HomeUiState(
            currentlyReading = reading.firstOrNull(),
            recentlyFinished = finished.take(4),
            totalBooks = all.size,
            booksFinished = finished.size,
            booksReading = reading.size,
            streak = streak,
            today = today,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}

/**
 * @property isLoading true until the library has been read once, so an empty
 * library is not shown ("0 books", the empty loan card) while it loads.
 * @property today The day [streak] was worked out for.
 */
data class HomeUiState(
    val currentlyReading: Book? = null,
    val recentlyFinished: List<Book> = emptyList(),
    val totalBooks: Int = 0,
    val booksFinished: Int = 0,
    val booksReading: Int = 0,
    val streak: ReadingStreak = ReadingStreak(),
    val today: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
)