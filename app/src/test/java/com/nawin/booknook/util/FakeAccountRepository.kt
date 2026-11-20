package com.nawin.booknook.util

import com.nawin.booknook.domain.model.Account
import com.nawin.booknook.domain.model.AccountError
import com.nawin.booknook.domain.model.AccountException
import com.nawin.booknook.domain.model.SignUpOutcome
import com.nawin.booknook.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory accounts. [registered] holds email → (password, name). Set [failWith]
 * to make the next actions fail, and [confirmEmail] to mimic a project that asks
 * for email confirmation.
 */
class FakeAccountRepository(
    override val isAvailable: Boolean = true,
) : AccountRepository {

    val registered = mutableMapOf<String, Pair<String, String?>>()
    var failWith: AccountError? = null
    var confirmEmail = false

    private val current = MutableStateFlow<Account?>(null)
    override val account: StateFlow<Account?> = current

    override suspend fun signUp(name: String, email: String, password: String): Result<SignUpOutcome> {
        failure<SignUpOutcome>()?.let { return it }
        if (email in registered) return Result.failure(AccountException(AccountError.EMAIL_TAKEN))
        registered[email] = password to name
        if (confirmEmail) return Result.success(SignUpOutcome.CONFIRM_EMAIL)
        current.value = Account(id = email, email = email, name = name)
        return Result.success(SignUpOutcome.SIGNED_IN)
    }

    override suspend fun signIn(email: String, password: String): Result<Account> {
        failure<Account>()?.let { return it }
        val (stored, name) = registered[email] ?: return Result.failure(AccountException(AccountError.INVALID_CREDENTIALS))
        if (stored != password) return Result.failure(AccountException(AccountError.INVALID_CREDENTIALS))
        return Result.success(Account(id = email, email = email, name = name).also { current.value = it })
    }

    override suspend fun signOut(): Result<Unit> {
        current.value = null
        return Result.success(Unit)
    }

    private fun <T> failure(): Result<T>? = failWith?.let { Result.failure(AccountException(it)) }
}
