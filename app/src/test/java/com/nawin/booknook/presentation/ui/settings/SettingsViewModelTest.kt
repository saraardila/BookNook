package com.nawin.booknook.presentation.ui.settings

import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.ReadingReminder
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.service.LibraryExporter
import com.nawin.booknook.domain.service.ReminderScheduler
import com.nawin.booknook.domain.usecase.GetAccountUseCase
import com.nawin.booknook.domain.usecase.SignOutUseCase
import com.nawin.booknook.domain.usecase.SignUpUseCase
import com.nawin.booknook.util.FakeAccountRepository
import com.nawin.booknook.util.MainDispatcherRule
import com.nawin.booknook.util.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Records what the view model asked the system to schedule. */
    private class FakeReminderScheduler : ReminderScheduler {
        var scheduled: Pair<Int, Int>? = null
        var cancelled = false

        override fun schedule(hour: Int, minute: Int) {
            scheduled = hour to minute
            cancelled = false
        }

        override fun cancel() {
            scheduled = null
            cancelled = true
        }
    }

    private val scheduler = FakeReminderScheduler()
    private var export: suspend (String) -> Int = { 0 }
    private val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())
    private val accounts = FakeAccountRepository()

    private fun viewModel() = SettingsViewModel(
        preferences,
        scheduler,
        LibraryExporter { export(it) },
        GetAccountUseCase(accounts),
        SignOutUseCase(accounts),
    )

    @Test
    fun `the account shows once signed in and signing out keeps the local profile`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        preferences.saveReaderName("Sara")
        SignUpUseCase(accounts)("Sara", "sara@example.com", "librosylibros")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.accountAvailable)
        assertEquals("sara@example.com", viewModel.uiState.value.account?.email)

        viewModel.signOut()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.account)
        assertEquals("Sara", viewModel.uiState.value.profile.name)
    }

    @Test
    fun `turning the reminder on schedules it at the saved time`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.setReminderEnabled(true)
        advanceUntilIdle()

        assertEquals(ReadingReminder(enabled = true, hour = 21, minute = 30), preferences.userPreferences.first().reminder)
        assertEquals(21 to 30, scheduler.scheduled)
    }

    @Test
    fun `changing the time of an active reminder reschedules it`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.setReminderEnabled(true)
        advanceUntilIdle()

        viewModel.setReminderTime(8, 15)
        advanceUntilIdle()

        assertEquals(8 to 15, scheduler.scheduled)
    }

    @Test
    fun `changing the time of a reminder that is off keeps it off`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.setReminderTime(8, 15)
        advanceUntilIdle()

        assertEquals(ReadingReminder(enabled = false, hour = 8, minute = 15), preferences.userPreferences.first().reminder)
        assertNull(scheduler.scheduled)
        assertTrue(scheduler.cancelled)
    }

    @Test
    fun `the name is saved without surrounding spaces`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.setReaderName("  Sara ")
        advanceUntilIdle()

        assertEquals("Sara", preferences.userPreferences.first().profile.name)
    }

    @Test
    fun `the chosen theme reaches the screen`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.setTheme(ShelfTheme.COWBOY)
        advanceUntilIdle()

        assertEquals(ShelfTheme.COWBOY, viewModel.uiState.value.theme)
    }

    @Test
    fun `a finished export reports how many books it wrote`() = runTest(mainDispatcherRule.dispatcher) {
        export = { 3 }
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.exportLibrary("content://downloads/booknook.csv")
        advanceUntilIdle()

        assertEquals(ExportStatus.Done(3), viewModel.uiState.value.exportStatus)
    }

    @Test
    fun `a failed export says so instead of crashing`() = runTest(mainDispatcherRule.dispatcher) {
        export = { throw IOException("disk full") }
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.exportLibrary("content://downloads/booknook.csv")
        advanceUntilIdle()

        assertEquals(ExportStatus.Failed, viewModel.uiState.value.exportStatus)
    }
}
