package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.repository.AccountRepository

/** Signs out on this device; the library stays on the phone. */
class SignOutUseCase(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.signOut()
}
