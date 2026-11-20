package com.nawin.booknook.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.domain.model.ReadingReminder
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.model.StoryHook
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
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val READER_NAME = stringPreferencesKey("reader_name")
        val MEMBER_SINCE = longPreferencesKey("member_since")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val FAVOURITE_SHELF = stringPreferencesKey("favourite_shelf")
        val STORY_HOOKS = stringSetPreferencesKey("story_hooks")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val defaults = UserPreferences()
            UserPreferences(
                onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: defaults.onboardingDone,
                // Values from older versions (the Botanical/Summer/… themes) fall back to the default.
                theme = enumOrDefault(prefs[Keys.THEME], defaults.theme),
                reminder = ReadingReminder(
                    enabled = prefs[Keys.REMINDER_ENABLED] ?: defaults.reminder.enabled,
                    hour = prefs[Keys.REMINDER_HOUR] ?: defaults.reminder.hour,
                    minute = prefs[Keys.REMINDER_MINUTE] ?: defaults.reminder.minute,
                ),
                profile = ReaderProfile(
                    name = prefs[Keys.READER_NAME].orEmpty(),
                    memberSince = prefs[Keys.MEMBER_SINCE],
                    favouriteShelf = prefs[Keys.FAVOURITE_SHELF]?.let { enumOrNull<ShelfTheme>(it) },
                    hooks = prefs[Keys.STORY_HOOKS].orEmpty().mapNotNullTo(mutableSetOf()) { enumOrNull<StoryHook>(it) },
                ),
            )
        }

    suspend fun saveTheme(theme: ShelfTheme) {
        dataStore.edit { it[Keys.THEME] = theme.name }
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

    /**
     * Saves the outcome of the welcome test and marks the welcome flow as done.
     * The favourite shelf also becomes the app theme; the reader can change it later.
     */
    suspend fun saveQuizResult(favouriteShelf: ShelfTheme, hooks: Set<StoryHook>) {
        dataStore.edit {
            it[Keys.FAVOURITE_SHELF] = favouriteShelf.name
            it[Keys.STORY_HOOKS] = hooks.mapTo(mutableSetOf()) { hook -> hook.name }
            it[Keys.THEME] = favouriteShelf.name
        }
    }

    /** Marks the welcome flow as finished (or skipped), so it is not shown again. */
    suspend fun completeOnboarding() {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    /** Records [now] as the date the reader joined, only the first time it is called. */
    suspend fun ensureMemberSince(now: Long) {
        dataStore.edit { if (it[Keys.MEMBER_SINCE] == null) it[Keys.MEMBER_SINCE] = now }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(stored: String?, default: T): T =
        stored?.let { enumOrNull<T>(it) } ?: default

    /** Null for names this version does not know (renamed or removed values). */
    private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? =
        enumValues<T>().firstOrNull { it.name == name }
}
