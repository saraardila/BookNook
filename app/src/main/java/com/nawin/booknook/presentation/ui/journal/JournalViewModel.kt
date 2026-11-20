package com.nawin.booknook.presentation.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.JournalEntry
import com.nawin.booknook.domain.model.ReadingMood
import com.nawin.booknook.domain.usecase.AddNoteUseCase
import com.nawin.booknook.domain.usecase.GetJournalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.YearMonth

/** Feeds the diary: its entries split by month, and loose thoughts written from it. */
class JournalViewModel(
    getJournalUseCase: GetJournalUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val clock: Clock,
) : ViewModel() {

    /** The month she picked, or null for the newest one with entries. */
    private val chosenMonth = MutableStateFlow<YearMonth?>(null)
    private val showSheet = MutableStateFlow(false)

    val uiState: StateFlow<JournalUiState> = combine(getJournalUseCase(), chosenMonth, showSheet) { entries, chosen, sheet ->
        // Entries come newest first, so the months do too.
        val byMonth = entries.groupBy { YearMonth.from(Instant.ofEpochMilli(it.date).atZone(clock.zone)) }
        val month = chosen?.takeIf { it in byMonth } ?: byMonth.keys.firstOrNull()
        JournalUiState(
            isLoading = false,
            months = byMonth.keys.toList(),
            selectedMonth = month,
            entries = byMonth[month].orEmpty(),
            currentMonth = YearMonth.now(clock),
            showThoughtSheet = sheet,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JournalUiState())

    fun selectMonth(month: YearMonth) {
        chosenMonth.value = month
    }

    fun showThoughtSheet(show: Boolean) {
        showSheet.value = show
    }

    /** Saves a thought that belongs to no book, and shows this month so she sees it. */
    fun addThought(mood: ReadingMood?, content: String) {
        showSheet.value = false
        if (content.isBlank() && mood == null) return
        val note = BookNote(bookId = null, chapter = null, content = content.trim(), page = null, mood = mood, createdAt = clock.millis())
        viewModelScope.launch { addNoteUseCase(note) }
        chosenMonth.value = YearMonth.now(clock)
    }
}

/**
 * @property months Months with at least one entry, newest first.
 * @property currentMonth Used to leave the year out of this year's month names.
 */
data class JournalUiState(
    val isLoading: Boolean = true,
    val months: List<YearMonth> = emptyList(),
    val selectedMonth: YearMonth? = null,
    val entries: List<JournalEntry> = emptyList(),
    val currentMonth: YearMonth? = null,
    val showThoughtSheet: Boolean = false,
)
