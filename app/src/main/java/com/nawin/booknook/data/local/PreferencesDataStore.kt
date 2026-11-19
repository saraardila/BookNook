package com.nawin.booknook.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nawin.booknook.domain.model.JournalInkColor
import com.nawin.booknook.domain.model.JournalStyle
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.domain.model.ReadingReminder
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.model.UserPreferences
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
        val THEME = stringPreferencesKey("theme")
        val JOURNAL_STYLE = stringPreferencesKey("journal_style")
        val INK_COLOR = stringPreferencesKey("ink_color")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val READER_NAME = stringPreferencesKey("reader_name")
        val MEMBER_SINCE = longPreferencesKey("member_since")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val defaults = UserPreferences()
            UserPreferences(
                // Values from older versions (the Botanical/Summer/… themes) fall back to the default.
                theme = enumOrDefault(prefs[Keys.THEME], defaults.theme),
                journalStyle = enumOrDefault(prefs[Keys.JOURNAL_STYLE], defaults.journalStyle),
                journalInkColor = enumOrDefault(prefs[Keys.INK_COLOR], defaults.journalInkColor),
                reminder = ReadingReminder(
                    enabled = prefs[Keys.REMINDER_ENABLED] ?: defaults.reminder.enabled,
                    hour = prefs[Keys.REMINDER_HOUR] ?: defaults.reminder.hour,
                    minute = prefs[Keys.REMINDER_MINUTE] ?: defaults.reminder.minute,
                ),
                profile = ReaderProfile(
                    name = prefs[Keys.READER_NAME].orEmpty(),
                    memberSince = prefs[Keys.MEMBER_SINCE],
                ),
            )
        }

    suspend fun saveTheme(theme: ShelfTheme) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun saveJournalStyle(style: JournalStyle) {
        dataStore.edit { it[Keys.JOURNAL_STYLE] = style.name }
    }

    suspend fun saveInkColor(color: JournalInkColor) {
        dataStore.edit { it[Keys.INK_COLOR] = color.name }
    }

    suspend fun saveReminder(reminder: ReadingReminder) {
        dataStore.edit {
            it[Keys.REMINDER_ENABLED] = reminder.enabled
            it[Keys.REMINDER_HOUR] = reminder.hour
            it[Keys.REMINDER_MINUTE] = reminder.minute
        }
    }

    suspend fun saveReaderName(name: String) {
        dataStore.edit { it[Keys.READER_NAME] = name }
    }

    /** Records [now] as the date the reader joined, only the first time it is called. */
    suspend fun ensureMemberSince(now: Long) {
        dataStore.edit { if (it[Keys.MEMBER_SINCE] == null) it[Keys.MEMBER_SINCE] = now }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(stored: String?, default: T): T =
        stored?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
}
