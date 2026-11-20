package com.nawin.booknook.presentation.ui.onboarding

import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.ReadingPlan
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.model.StoryEnding
import com.nawin.booknook.domain.model.StoryHook
import com.nawin.booknook.domain.model.StoryWorld
import com.nawin.booknook.domain.usecase.GetAccountUseCase
import com.nawin.booknook.util.FakeAccountRepository
import com.nawin.booknook.util.InMemoryPreferencesDataStore
import com.nawin.booknook.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())
    // Lazy: viewModelScope must be created after the rule has replaced Dispatchers.Main.
    // Most tests use a build without accounts, so the test follows the welcome page directly.
    private val viewModel by lazy { OnboardingViewModel(preferences, GetAccountUseCase(FakeAccountRepository(isAvailable = false))) }

    @Test
    fun `a question cannot be left unanswered`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.next() // welcome → question 1
        advanceUntilIdle()

        assertEquals(OnboardingPage.PLAN, viewModel.uiState.value.page)
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.selectPlan(ReadingPlan.TRAIN)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun `finishing the test saves the favourite shelf and dresses the app in it`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.next()
        viewModel.selectPlan(ReadingPlan.CANDLELIT_TAROT)
        viewModel.next()
        viewModel.selectWorld(StoryWorld.ENCHANTED_CABIN)
        viewModel.next()
        viewModel.toggleHook(StoryHook.WITCHES)
        viewModel.toggleHook(StoryHook.MYSTERY)
        viewModel.toggleHook(StoryHook.MYSTERY) // unticked again
        viewModel.next()
        viewModel.selectEnding(StoryEnding.OPEN)
        viewModel.next()
        advanceUntilIdle()

        val stored = preferences.userPreferences.first()
        assertEquals(OnboardingPage.RESULT, viewModel.uiState.value.page)
        assertEquals(ShelfTheme.WITCHY, stored.profile.favouriteShelf)
        assertEquals(setOf(StoryHook.WITCHES), stored.profile.hooks)
        assertEquals(ShelfTheme.WITCHY, stored.theme)
        assertFalse(stored.onboardingDone)
    }

    @Test
    fun `back walks the pages and stops at the welcome page`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.next()
        advanceUntilIdle()

        assertTrue(viewModel.back())
        advanceUntilIdle()
        assertEquals(OnboardingPage.WELCOME, viewModel.uiState.value.page)
        assertFalse(viewModel.back())
    }

    @Test
    fun `the name is saved trimmed and leads to the card`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.saveName("  Sara ")
        advanceUntilIdle()

        assertEquals("Sara", preferences.userPreferences.first().profile.name)
        assertEquals(OnboardingPage.CARD, viewModel.uiState.value.page)
    }

    @Test
    fun `skipping marks the welcome flow as done without a favourite shelf`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.finish()
        advanceUntilIdle()

        val stored = preferences.userPreferences.first()
        assertTrue(stored.onboardingDone)
        assertEquals(null, stored.profile.favouriteShelf)
        assertEquals(ShelfTheme.STANDARD, stored.theme)
    }

    @Test
    fun `with accounts, the account page sits between the welcome page and the test`() = runTest(mainDispatcherRule.dispatcher) {
        val withAccounts = OnboardingViewModel(preferences, GetAccountUseCase(FakeAccountRepository(isAvailable = true)))
        backgroundScope.launch { withAccounts.uiState.collect {} }

        withAccounts.next()
        advanceUntilIdle()
        assertEquals(OnboardingPage.ACCOUNT, withAccounts.uiState.value.page)

        withAccounts.next()
        advanceUntilIdle()
        assertEquals(OnboardingPage.PLAN, withAccounts.uiState.value.page)

        withAccounts.back()
        advanceUntilIdle()
        assertEquals(OnboardingPage.ACCOUNT, withAccounts.uiState.value.page)
    }
}
