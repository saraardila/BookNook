package com.nawin.booknook.presentation.ui.readingcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.ReadingCard
import com.nawin.booknook.domain.usecase.GetReadingCardUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Feeds the reading card screen; [ReadingCardUiState.card] is null until the library is read. */
class ReadingCardViewModel(
    getReadingCardUseCase: GetReadingCardUseCase,
) : ViewModel() {

    val uiState: StateFlow<ReadingCardUiState> = getReadingCardUseCase()
        .map { ReadingCardUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReadingCardUiState())
}

data class ReadingCardUiState(val card: ReadingCard? = null)
