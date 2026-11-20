package com.nawin.booknook.presentation.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App-wide state for the activity: the preferences that style every screen and
 * decide whether the welcome flow is shown. Null until DataStore has been read,
 * so the splash screen can wait for it instead of showing the defaults first.
 */
class MainViewModel(
    private val preferencesDataStore: PreferencesDataStore,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences?> = preferencesDataStore.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    init {
        // The local profile's "reader since" date is the first launch.
        viewModelScope.launch { preferencesDataStore.ensureMemberSince(System.currentTimeMillis()) }
    }
}
