package com.nawin.booknook.domain.model

/**
 * The signed-in reader's account.
 *
 * @property name The name given when the account was created; null for accounts made without one.
 */
data class Account(val id: String, val email: String, val name: String?)

/** Why an account action failed, so the screen can tell the reader what to do. */
enum class AccountError {
    INVALID_CREDENTIALS,
    EMAIL_TAKEN,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    EMAIL_NOT_CONFIRMED,
    TOO_MANY_ATTEMPTS,
    NO_CONNECTION,

    /** The app was built without an account server. */
    NOT_AVAILABLE,
    UNKNOWN,
}

/** An account action that failed for [error]. */
class AccountException(val error: AccountError, cause: Throwable? = null) : Exception(error.name, cause)

/** How creating an account ended. */
enum class SignUpOutcome {
    /** The account is ready and signed in. */
    SIGNED_IN,

    /** The server sent a confirmation email; the reader signs in after confirming. */
    CONFIRM_EMAIL,
}
