package com.nawin.booknook.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.DailyQuote
import com.nawin.booknook.domain.model.HomeBlock
import com.nawin.booknook.domain.model.HomeLayout
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.model.ReadingStreak
import com.nawin.booknook.domain.model.ShelfLicence
import com.nawin.booknook.domain.usecase.GetDailyQuoteUseCase
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import com.nawin.booknook.domain.usecase.GetReadingStreakUseCase
import com.nawin.booknook.domain.usecase.GetShelfLicencesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate

class HomeViewModel(
    getMyLibraryUseCase: GetMyLibraryUseCase,
    getReadingStreakUseCase: GetReadingStreakUseCase,
    getShelfLicencesUseCase: GetShelfLicencesUseCase,
    getDailyQuoteUseCase: GetDailyQuoteUseCase,
    preferences: PreferencesDataStore,
    clock: Clock,
) : ViewModel() {

    // Read when Home is created; a new day shows the next time the screen is opened.
    private val today = LocalDate.now(clock)

    private val library = combine(
        getMyLibraryUseCase(ReadingStatus.READING),
        getMyLibraryUseCase(ReadingStatus.FINISHED),
        getMyLibraryUseCase(),
    ) { reading, finished, all -> Triple(reading, finished, all) }

    /** What the blocks other than the library ones show, and the order they go in. */
    private val extras = combine(
        getReadingStreakUseCase(today.toEpochDay()),
        getShelfLicencesUseCase(),
        getDailyQuoteUseCase(today.toEpochDay()),
        preferences.userPreferences.map { it.homeLayout },
    ) { streak, licences, quote, layout -> HomeExtras(streak, licences, quote, layout) }

    val uiState: StateFlow<HomeUiState> = combine(library, extras) { (reading, finished, all), extras ->
        HomeUiState(
            currentlyReading = reading.firstOrNull(),
            recentlyFinished = finished.take(4),
            totalBooks = all.size,
            booksFinished = finished.size,
            booksReading = reading.size,
            streak = extras.streak,
            licences = extras.licences,
            quote = extras.quote,
            blocks = extras.layout.visible,
            today = today,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private data class HomeExtras(
        val streak: ReadingStreak,
        val licences: List<ShelfLicence>,
        val quote: DailyQuote?,
        val layout: HomeLayout,
    )
}

/**
 * @property isLoading true until the library has been read once, so an empty
 * library is not shown ("0 books", the empty loan card) while it loads.
 * @property today The day [streak] was worked out for.
 * @property blocks The blocks she chose to show, top to bottom.
 * @property quote Today's line from her notes; null when she has none.
 */
data class HomeUiState(
    val currentlyReading: Book? = null,
    val recentlyFinished: List<Book> = emptyList(),
    val totalBooks: Int = 0,
    val booksFinished: Int = 0,
    val booksReading: Int = 0,
    val streak: ReadingStreak = ReadingStreak(),
    val licences: List<ShelfLicence> = emptyList(),
    val quote: DailyQuote? = null,
    val blocks: List<HomeBlock> = HomeLayout().visible,
    val today: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
)
