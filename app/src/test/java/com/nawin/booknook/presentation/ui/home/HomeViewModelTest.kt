package com.nawin.booknook.presentation.ui.home

import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.HomeBlock
import com.nawin.booknook.domain.model.HomeLayout
import com.nawin.booknook.domain.usecase.GetDailyQuoteUseCase
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import com.nawin.booknook.domain.usecase.GetReadingStreakUseCase
import com.nawin.booknook.domain.usecase.GetShelfLicencesUseCase
import com.nawin.booknook.util.FakeBookRepository
import com.nawin.booknook.util.FakeReadingLogRepository
import com.nawin.booknook.util.InMemoryPreferencesDataStore
import com.nawin.booknook.util.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookRepository()
    private val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())

    // Lazy so it is built after the rule has replaced Dispatchers.Main.
    private val viewModel by lazy {
        HomeViewModel(
            GetMyLibraryUseCase(repository),
            GetReadingStreakUseCase(FakeReadingLogRepository()),
            GetShelfLicencesUseCase(repository),
            GetDailyQuoteUseCase(repository),
            preferences,
            Clock.systemUTC(),
        )
    }

    /** Before the first read, Home must not look like an empty library ("0 books"). */
    @Test
    fun `is loading until the library has been read, then shows an empty library`() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)

        val state = viewModel.uiState
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { state.collect() }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, viewModel.uiState.value.totalBooks)
    }

    @Test
    fun `the blocks follow the saved layout`() = runTest {
        preferences.saveHomeLayout(
            HomeLayout(order = HomeBlock.entries.reversed(), hidden = setOf(HomeBlock.LICENCES)),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertEquals(
            listOf(HomeBlock.QUOTE_OF_THE_DAY, HomeBlock.RECENTLY_FINISHED, HomeBlock.STREAK, HomeBlock.CURRENTLY_READING),
            viewModel.uiState.value.blocks,
        )
    }
}
