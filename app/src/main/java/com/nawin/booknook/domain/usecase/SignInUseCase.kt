package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.Account
import com.nawin.booknook.domain.repository.AccountRepository

/** Signs in with an existing account. */
class SignInUseCase(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<Account> =
        repository.signIn(email.trim(), password)
}
