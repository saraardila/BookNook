package com.nawin.booknook.presentation.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.AccountError
import com.nawin.booknook.domain.model.AccountException
import com.nawin.booknook.domain.model.SignUpOutcome
import com.nawin.booknook.domain.usecase.SignInUseCase
import com.nawin.booknook.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The two tabs of the account form. */
enum class AccountMode { SIGN_UP, SIGN_IN }

/** How the form ended. */
enum class AccountDone {
    SIGNED_IN,

    /** The account was created but must be confirmed from the email before signing in. */
    CHECK_EMAIL,
}

/**
 * @property error Why the last attempt failed; cleared as soon as the reader edits the form.
 * @property done Set once, when the form has finished its job.
 */
data class AccountUiState(
    val mode: AccountMode = AccountMode.SIGN_UP,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isWorking: Boolean = false,
    val error: AccountError? = null,
    val done: AccountDone? = null,
) {
    val canSubmit: Boolean
        get() = !isWorking &&
            email.isNotBlank() &&
            password.length >= MIN_PASSWORD_LENGTH &&
            (mode == AccountMode.SIGN_IN || name.isNotBlank())

    companion object {
        /** Supabase's default minimum; the server still has the final word. */
        const val MIN_PASSWORD_LENGTH = 6
    }
}

/**
 * Create an account or sign in (P03). Used by the welcome flow and by
 * Settings → Account. The name typed here also becomes the local profile name.
 */
class AccountViewModel(
    private val signUp: SignUpUseCase,
    private val signIn: SignInUseCase,
    private val preferences: PreferencesDataStore,
) : ViewModel() {

    private val state = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = state.asStateFlow()

    init {
        // Start from the name she already gave, if any (Settings, or back from the test).
        viewModelScope.launch {
            val stored = preferences.userPreferences.first().profile.name
            state.update { if (it.name.isEmpty()) it.copy(name = stored) else it }
        }
    }

    fun selectMode(mode: AccountMode) = state.update { it.copy(mode = mode, error = null) }

    fun updateName(name: String) = state.update { it.copy(name = name, error = null) }

    fun updateEmail(email: String) = state.update { it.copy(email = email, error = null) }

    fun updatePassword(password: String) = state.update { it.copy(password = password, error = null) }

    fun submit() {
        val form = state.value
        if (!form.canSubmit) return
        if (!form.email.looksLikeEmail()) {
            state.update { it.copy(error = AccountError.INVALID_EMAIL) }
            return
        }
        state.update { it.copy(isWorking = true, error = null) }
        viewModelScope.launch {
            val result = when (form.mode) {
                AccountMode.SIGN_UP -> signUp(form.name, form.email, form.password).map { outcome ->
                    preferences.saveReaderName(form.name.trim())
                    if (outcome == SignUpOutcome.SIGNED_IN) AccountDone.SIGNED_IN else AccountDone.CHECK_EMAIL
                }
                AccountMode.SIGN_IN -> signIn(form.email, form.password).map { account ->
                    // A new phone: bring the account's name into the local profile, without overwriting one she typed.
                    val stored = preferences.userPreferences.first().profile.name
                    if (stored.isBlank()) account.name?.let { preferences.saveReaderName(it) }
                    AccountDone.SIGNED_IN
                }
            }
            state.update { current ->
                result.fold(
                    onSuccess = { done -> current.copy(isWorking = false, password = "", done = done) },
                    onFailure = { e ->
                        current.copy(isWorking = false, error = (e as? AccountException)?.error ?: AccountError.UNKNOWN)
                    },
                )
            }
        }
    }

    private fun String.looksLikeEmail(): Boolean {
        val trimmed = trim()
        val at = trimmed.indexOf('@')
        return at > 0 && trimmed.indexOf('.', startIndex = at) > at + 1 && !trimmed.endsWith('.')
    }
}
