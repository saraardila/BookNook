package com.nawin.booknook.presentation.ui.account

import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.AccountError
import com.nawin.booknook.domain.usecase.SignInUseCase
import com.nawin.booknook.domain.usecase.SignUpUseCase
import com.nawin.booknook.util.FakeAccountRepository
import com.nawin.booknook.util.InMemoryPreferencesDataStore
import com.nawin.booknook.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accounts = FakeAccountRepository()
    private val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())
    private val viewModel by lazy { AccountViewModel(SignUpUseCase(accounts), SignInUseCase(accounts), preferences) }

    @Test
    fun `creating an account signs in and keeps the name in the local profile`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.updateName("  Sara ")
        viewModel.updateEmail("sara@example.com")
        viewModel.updatePassword("librosylibros")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(AccountDone.SIGNED_IN, viewModel.uiState.value.done)
        assertEquals("Sara", preferences.userPreferences.first().profile.name)
        assertEquals("sara@example.com", accounts.account.value?.email)
        assertEquals("", viewModel.uiState.value.password)
    }

    @Test
    fun `the form cannot be sent until it is complete and a bad email is caught here`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.updateEmail("sara")
        viewModel.updatePassword("12345")
        assertFalse(viewModel.uiState.value.canSubmit) // no name, password too short

        viewModel.updateName("Sara")
        viewModel.updatePassword("123456")
        assertTrue(viewModel.uiState.value.canSubmit)

        viewModel.submit()
        advanceUntilIdle()
        assertEquals(AccountError.INVALID_EMAIL, viewModel.uiState.value.error)
        assertTrue(accounts.registered.isEmpty())

        viewModel.updateEmail("sara@example.com")
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `a wrong password is reported and nothing is signed in`() = runTest(mainDispatcherRule.dispatcher) {
        accounts.registered["sara@example.com"] = "librosylibros" to "Sara"
        viewModel.selectMode(AccountMode.SIGN_IN)
        viewModel.updateEmail("sara@example.com")
        viewModel.updatePassword("equivocada")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(AccountError.INVALID_CREDENTIALS, viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.done)
        assertFalse(viewModel.uiState.value.isWorking)
    }

    @Test
    fun `signing in on a new phone brings the account name but never overwrites one she typed`() = runTest(mainDispatcherRule.dispatcher) {
        accounts.registered["sara@example.com"] = "librosylibros" to "Sara"
        viewModel.selectMode(AccountMode.SIGN_IN)
        viewModel.updateEmail("sara@example.com")
        viewModel.updatePassword("librosylibros")
        viewModel.submit()
        advanceUntilIdle()
        assertEquals("Sara", preferences.userPreferences.first().profile.name)

        preferences.saveReaderName("Sarita")
        accounts.signOut()
        viewModel.updatePassword("librosylibros") // cleared after the first sign-in
        viewModel.submit()
        advanceUntilIdle()
        assertEquals("Sarita", preferences.userPreferences.first().profile.name)
    }

    @Test
    fun `a project with email confirmation asks her to check her inbox`() = runTest(mainDispatcherRule.dispatcher) {
        accounts.confirmEmail = true
        viewModel.updateName("Sara")
        viewModel.updateEmail("sara@example.com")
        viewModel.updatePassword("librosylibros")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(AccountDone.CHECK_EMAIL, viewModel.uiState.value.done)
        assertNull(accounts.account.value)
    }
}
