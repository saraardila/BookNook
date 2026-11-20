package com.nawin.booknook.data.account

import com.nawin.booknook.domain.model.Account
import com.nawin.booknook.domain.model.AccountError
import com.nawin.booknook.domain.model.AccountException
import com.nawin.booknook.domain.model.SignUpOutcome
import com.nawin.booknook.domain.repository.AccountRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.IOException

/**
 * Email and password accounts on Supabase Auth. With no [client] (a build
 * without a Supabase project) there is never an account and every action fails
 * with [AccountError.NOT_AVAILABLE].
 */
class SupabaseAccountRepository(
    private val client: SupabaseClient?,
) : AccountRepository {

    override val isAvailable: Boolean get() = client != null

    override val account: Flow<Account?> = client?.auth?.let { auth ->
        auth.sessionStatus
            // While the stored session loads, the answer is not known yet: wait instead of saying "signed out".
            .filterNot { it is SessionStatus.Initializing }
            .map { status ->
                when (status) {
                    is SessionStatus.Authenticated -> status.session.user?.toAccount()
                    // Offline at start: the token could not be refreshed, but the reader is still signed in.
                    is SessionStatus.RefreshFailure -> auth.currentUserOrNull()?.toAccount()
                    else -> null
                }
            }
            .distinctUntilChanged()
    } ?: flowOf(null)

    override suspend fun signUp(name: String, email: String, password: String): Result<SignUpOutcome> = attempt { auth ->
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject { put(NAME_KEY, name) }
        }
        // With email confirmation on, Supabase creates the user but opens no session until it is confirmed.
        if (auth.currentSessionOrNull() != null) SignUpOutcome.SIGNED_IN else SignUpOutcome.CONFIRM_EMAIL
    }

    override suspend fun signIn(email: String, password: String): Result<Account> = attempt { auth ->
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        auth.currentUserOrNull()?.toAccount() ?: throw AccountException(AccountError.UNKNOWN)
    }

    override suspend fun signOut(): Result<Unit> = attempt { auth ->
        try {
            auth.signOut()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline: the server could not be told, but this device must still forget the session.
            auth.clearSession()
        }
    }

    /** Runs [block] and turns its failures into [AccountException]s, letting cancellation through. */
    private suspend fun <T> attempt(block: suspend (Auth) -> T): Result<T> {
        val auth = client?.auth ?: return Result.failure(AccountException(AccountError.NOT_AVAILABLE))
        return try {
            Result.success(block(auth))
        } catch (e: CancellationException) {
            throw e
        } catch (e: AccountException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AccountException(e.toAccountError(), e))
        }
    }

    private companion object {
        /** Key of the reader's name in the Supabase user metadata. */
        const val NAME_KEY = "name"

        fun UserInfo.toAccount() = Account(
            id = id,
            email = email.orEmpty(),
            name = userMetadata?.get(NAME_KEY)?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() },
        )
    }
}

/** Maps what Supabase or the network threw to the reasons the screen can explain. */
internal fun Throwable.toAccountError(): AccountError = when (this) {
    is AuthWeakPasswordException -> AccountError.WEAK_PASSWORD
    is AuthRestException -> when (errorCode) {
        AuthErrorCode.InvalidCredentials -> AccountError.INVALID_CREDENTIALS
        AuthErrorCode.EmailExists, AuthErrorCode.UserAlreadyExists -> AccountError.EMAIL_TAKEN
        AuthErrorCode.WeakPassword -> AccountError.WEAK_PASSWORD
        AuthErrorCode.EmailAddressInvalid -> AccountError.INVALID_EMAIL
        AuthErrorCode.EmailNotConfirmed -> AccountError.EMAIL_NOT_CONFIRMED
        AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit -> AccountError.TOO_MANY_ATTEMPTS
        else -> AccountError.UNKNOWN
    }
    // HttpRequestException (no connection, timeout) is an IOException too.
    is IOException -> AccountError.NO_CONNECTION
    else -> AccountError.UNKNOWN
}
