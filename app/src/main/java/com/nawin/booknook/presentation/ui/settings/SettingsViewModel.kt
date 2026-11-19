package com.nawin.booknook.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.domain.model.ReadingReminder
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.service.LibraryExporter
import com.nawin.booknook.domain.service.ReminderScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Outcome of the last "export my books", shown under the account card. */
sealed interface ExportStatus {
    data object Running : ExportStatus
    data class Done(val bookCount: Int) : ExportStatus
    data object Failed : ExportStatus
}

data class SettingsUiState(
    val theme: ShelfTheme = ShelfTheme.STANDARD,
    val reminder: ReadingReminder = ReadingReminder(),
    val profile: ReaderProfile = ReaderProfile(),
    val exportStatus: ExportStatus? = null,
)

/**
 * Settings tab: shelf theme, reading reminder and the local profile. The app
 * language is not here: AppCompat stores it and recreates the screen itself.
 */
class SettingsViewModel(
    private val preferencesDataStore: PreferencesDataStore,
    private val reminderScheduler: ReminderScheduler,
    private val libraryExporter: LibraryExporter,
) : ViewModel() {

    private val exportStatus = MutableStateFlow<ExportStatus?>(null)

    val uiState: StateFlow<SettingsUiState> =
        combine(preferencesDataStore.userPreferences, exportStatus) { preferences, export ->
            SettingsUiState(preferences.theme, preferences.reminder, preferences.profile, export)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SettingsUiState())

    fun setTheme(theme: ShelfTheme) {
        viewModelScope.launch { preferencesDataStore.saveTheme(theme) }
    }

    /** Turns the reminder on or off. Callers ask for the notification permission first. */
    fun setReminderEnabled(enabled: Boolean) = updateReminder { it.copy(enabled = enabled) }

    fun setReminderTime(hour: Int, minute: Int) = updateReminder { it.copy(hour = hour, minute = minute) }

    fun setReaderName(name: String) {
        viewModelScope.launch { preferencesDataStore.saveReaderName(name.trim()) }
    }

    /** Writes the library to [destination] (a document the reader picked) and reports the result. */
    fun exportLibrary(destination: String) {
        exportStatus.value = ExportStatus.Running
        viewModelScope.launch {
            exportStatus.value = try {
                ExportStatus.Done(libraryExporter.export(destination))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ExportStatus.Failed
            }
        }
    }

    /** Saves the new reminder and brings the system schedule in line with it. */
    private fun updateReminder(change: (ReadingReminder) -> ReadingReminder) {
        viewModelScope.launch {
            val reminder = change(preferencesDataStore.userPreferences.first().reminder)
            preferencesDataStore.saveReminder(reminder)
            if (reminder.enabled) reminderScheduler.schedule(reminder.hour, reminder.minute) else reminderScheduler.cancel()
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
