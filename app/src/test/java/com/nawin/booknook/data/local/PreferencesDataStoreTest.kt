package com.nawin.booknook.data.local

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.nawin.booknook.domain.model.HomeBlock
import com.nawin.booknook.domain.model.HomeLayout
import com.nawin.booknook.domain.model.ReadingReminder
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.model.StoryHook
import com.nawin.booknook.domain.model.UserPreferences
import com.nawin.booknook.util.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesDataStoreTest {

    @Test
    fun `nothing stored gives the defaults`() = runTest {
        val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())

        assertEquals(UserPreferences(), preferences.userPreferences.first())
    }

    @Test
    fun `a theme from an older version falls back to the standard one`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        dataStore.edit { it[stringPreferencesKey("theme")] = "BOTANICAL" }

        assertEquals(ShelfTheme.STANDARD, PreferencesDataStore(dataStore).userPreferences.first().theme)
    }

    @Test
    fun `theme, reminder and name are read back`() = runTest {
        val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())

        preferences.saveTheme(ShelfTheme.WITCHY)
        preferences.saveReminder(ReadingReminder(enabled = true, hour = 7, minute = 5))
        preferences.saveReaderName("Sara")

        val stored = preferences.userPreferences.first()
        assertEquals(ShelfTheme.WITCHY, stored.theme)
        assertEquals(ReadingReminder(enabled = true, hour = 7, minute = 5), stored.reminder)
        assertEquals("Sara", stored.profile.name)
    }

    @Test
    fun `the test result is read back and unknown hooks are dropped`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val preferences = PreferencesDataStore(dataStore)

        preferences.saveQuizResult(ShelfTheme.COWBOY, setOf(StoryHook.SMALL_TOWN))
        dataStore.edit { it[stringSetPreferencesKey("story_hooks")] = setOf("SMALL_TOWN", "REMOVED_IN_A_LATER_VERSION") }

        val profile = preferences.userPreferences.first().profile
        assertEquals(ShelfTheme.COWBOY, profile.favouriteShelf)
        assertEquals(setOf(StoryHook.SMALL_TOWN), profile.hooks)
    }

    @Test
    fun `the member-since date is only recorded once`() = runTest {
        val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())

        preferences.ensureMemberSince(1_000L)
        preferences.ensureMemberSince(2_000L)

        assertEquals(1_000L, preferences.userPreferences.first().profile.memberSince)
    }

    @Test
    fun `the home layout keeps its order and hidden blocks`() = runTest {
        val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())
        val layout = HomeLayout(order = HomeBlock.entries.shuffled(kotlin.random.Random(7)), hidden = setOf(HomeBlock.STREAK))

        preferences.saveHomeLayout(layout)

        assertEquals(layout, preferences.userPreferences.first().homeLayout)
    }
}
