package com.nawin.booknook.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nawin.booknook.presentation.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "booknook_prefs")

class PreferencesDataStore(
    private val context: Context
) {
    private object Keys {
        val THEME         = stringPreferencesKey("theme")
        val JOURNAL_STYLE = stringPreferencesKey("journal_style")
        val INK_COLOR     = stringPreferencesKey("ink_color")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data
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
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun saveJournalStyle(style: JournalStyle) {
        context.dataStore.edit { it[Keys.JOURNAL_STYLE] = style.name }
    }

    suspend fun saveInkColor(color: JournalInkColor) {
        context.dataStore.edit { it[Keys.INK_COLOR] = color.name }
    }
}