package com.nawin.booknook.presentation.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesDataStore.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setTheme(theme: BookTheme) {
        viewModelScope.launch { preferencesDataStore.saveTheme(theme) }
    }

    fun setJournalStyle(style: JournalStyle) {
        viewModelScope.launch { preferencesDataStore.saveJournalStyle(style) }
    }

    fun setInkColor(color: JournalInkColor) {
        viewModelScope.launch { preferencesDataStore.saveInkColor(color) }
    }
}