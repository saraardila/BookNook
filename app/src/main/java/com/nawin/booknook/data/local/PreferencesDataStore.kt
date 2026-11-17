package com.nawin.booknook.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nawin.booknook.presentation.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Takes a ready-made DataStore instead of a Context: creating it is the only
 * platform-specific part, and it lives in the Koin module.
 */
class PreferencesDataStore(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val THEME         = stringPreferencesKey("theme")
        val JOURNAL_STYLE = stringPreferencesKey("journal_style")
        val INK_COLOR     = stringPreferencesKey("ink_color")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            UserPreferences(
                theme = BookTheme.valueOf(
                    prefs[Keys.THEME] ?: BookTheme.BOTANICAL.name
                ),
                journalStyle = JournalStyle.valueOf(
                    prefs[Keys.JOURNAL_STYLE] ?: JournalStyle.DOTTED.name
                ),
                journalInkColor = JournalInkColor.valueOf(
                    prefs[Keys.INK_COLOR] ?: JournalInkColor.DARK.name
                )
            )
        }

    suspend fun saveTheme(theme: BookTheme) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun saveJournalStyle(style: JournalStyle) {
        dataStore.edit { it[Keys.JOURNAL_STYLE] = style.name }
    }

    suspend fun saveInkColor(color: JournalInkColor) {
        dataStore.edit { it[Keys.INK_COLOR] = color.name }
    }
}
