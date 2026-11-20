package com.nawin.booknook.presentation.ui.homeedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.HomeBlock
import com.nawin.booknook.domain.model.HomeLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The home editor (P11). Changes go to a draft; nothing is stored until she
 * taps "Save my home", so leaving with Back discards them.
 */
class EditHomeViewModel(
    private val preferences: PreferencesDataStore,
) : ViewModel() {

    private val draft = MutableStateFlow<HomeLayout?>(null)
    private val saved = MutableStateFlow(false)

    init {
        viewModelScope.launch { draft.value = preferences.userPreferences.first().homeLayout }
    }

    val uiState: StateFlow<EditHomeUiState> = combine(draft, saved) { layout, saved ->
        EditHomeUiState(layout = layout, saved = saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EditHomeUiState())

    fun toggle(block: HomeBlock) {
        draft.value = draft.value?.toggle(block)
    }

    fun move(from: Int, to: Int) {
        draft.value = draft.value?.move(from, to)
    }

    /**
     * Stores the draft; [EditHomeUiState.saved] turns true once it is written, so
     * the screen closes only then (closing first would cancel the write).
     */
    fun save() {
        val layout = draft.value ?: return
        viewModelScope.launch {
            preferences.saveHomeLayout(layout)
            saved.value = true
        }
    }
}

/**
 * @property layout The draft being edited; null while the stored one loads.
 * @property saved True once the draft has been stored and the screen can close.
 */
data class EditHomeUiState(
    val layout: HomeLayout? = null,
    val saved: Boolean = false,
)
