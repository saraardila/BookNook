package com.nawin.booknook.domain.repository

import com.nawin.booknook.domain.model.Account
import com.nawin.booknook.domain.model.SignUpOutcome
import kotlinx.coroutines.flow.Flow

/**
 * The reader's optional account. Failures come back as a failed [Result]
 * holding an [com.nawin.booknook.domain.model.AccountException].
 */
interface AccountRepository {

    /** False when the app was built without an account server; the account options are hidden then. */
    val isAvailable: Boolean

    /** The signed-in account, or null when there is none. */
    val account: Flow<Account?>

    suspend fun signUp(name: String, email: String, password: String): Result<SignUpOutcome>

    suspend fun signIn(email: String, password: String): Result<Account>

    /** Signs out on this device. The library stays on the phone. */
    suspend fun signOut(): Result<Unit>
}
