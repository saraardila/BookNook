package com.nawin.booknook.presentation.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.ReadingStreak
import com.nawin.booknook.domain.model.ShelfLicence
import com.nawin.booknook.domain.usecase.GetReadingStreakUseCase
import com.nawin.booknook.domain.usecase.GetShelfLicencesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate

/** Feeds the progress screen: the reading streak and the shelf licences. */
class ProgressViewModel(
    getReadingStreakUseCase: GetReadingStreakUseCase,
    getShelfLicencesUseCase: GetShelfLicencesUseCase,
    clock: Clock,
) : ViewModel() {

    private val today = LocalDate.now(clock)

    val uiState: StateFlow<ProgressUiState> = combine(
        getReadingStreakUseCase(today.toEpochDay()),
        getShelfLicencesUseCase(),
    ) { streak, licences ->
        ProgressUiState(streak = streak, licences = licences, today = today, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressUiState(today = today))
}

/**
 * @property today The day [streak] was worked out for.
 * @property isLoading true until both sources have answered once.
 */
data class ProgressUiState(
    val streak: ReadingStreak = ReadingStreak(),
    val licences: List<ShelfLicence> = emptyList(),
    val today: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
)
