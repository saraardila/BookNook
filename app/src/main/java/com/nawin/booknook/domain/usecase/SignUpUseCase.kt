package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.SignUpOutcome
import com.nawin.booknook.domain.repository.AccountRepository

/** Creates an account with the reader's name, email and password. */
class SignUpUseCase(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<SignUpOutcome> =
        repository.signUp(name.trim(), email.trim(), password)
}
