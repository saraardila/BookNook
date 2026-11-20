package com.nawin.booknook.presentation.ui.homeedit

import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.HomeBlock
import com.nawin.booknook.domain.model.HomeLayout
import com.nawin.booknook.util.InMemoryPreferencesDataStore
import com.nawin.booknook.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EditHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())
    private val viewModel by lazy { EditHomeViewModel(preferences) }

    @Test
    fun `changes stay in the draft until saved`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.move(from = 0, to = 1)
        viewModel.toggle(HomeBlock.QUOTE_OF_THE_DAY)
        advanceUntilIdle()

        val expected = HomeLayout().move(0, 1).toggle(HomeBlock.QUOTE_OF_THE_DAY)
        assertEquals(expected, viewModel.uiState.value.layout)
        assertEquals(HomeLayout(), preferences.userPreferences.first().homeLayout)
        assertFalse(viewModel.uiState.value.saved)

        viewModel.save()
        advanceUntilIdle()
        assertEquals(expected, preferences.userPreferences.first().homeLayout)
        assertTrue(viewModel.uiState.value.saved)
    }
}
