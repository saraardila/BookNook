package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.Account
import com.nawin.booknook.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

/** The signed-in account, and whether accounts exist at all in this build. */
class GetAccountUseCase(
    private val repository: AccountRepository,
) {
    val isAvailable: Boolean get() = repository.isAvailable

    operator fun invoke(): Flow<Account?> = repository.account
}
