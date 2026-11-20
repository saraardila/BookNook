package com.nawin.booknook.domain.model

/**
 * The look of the whole library, one per kind of shelf. [STANDARD] is the
 * default vanilla look; the rest follow the reader's favourite genre.
 */
enum class ShelfTheme {
    STANDARD,
    ROMANTASY,
    WITCHY,
    COWBOY,
    ROMANCE,
    THRILLER,
}

/** Daily nudge to read, at [hour]:[minute] local time. */
data class ReadingReminder(
    val enabled: Boolean = false,
    val hour: Int = DEFAULT_HOUR,
    val minute: Int = DEFAULT_MINUTE,
) {
    private companion object {
        const val DEFAULT_HOUR = 21
        const val DEFAULT_MINUTE = 30
    }
}

/**
 * The reader's local profile until accounts exist. [memberSince] is the first
 * launch, in epoch milliseconds; null only before it has been recorded.
 * [favouriteShelf] and [hooks] come from the welcome test; null and empty when
 * the reader skipped it.
 */
data class ReaderProfile(
    val name: String = "",
    val memberSince: Long? = null,
    val favouriteShelf: ShelfTheme? = null,
    val hooks: Set<StoryHook> = emptySet(),
)

/**
 * Everything the reader can change in Settings, plus whether the welcome flow
 * (test and library card) has been completed or skipped.
 */
data class UserPreferences(
    val onboardingDone: Boolean = false,
    val theme: ShelfTheme = ShelfTheme.STANDARD,
    val reminder: ReadingReminder = ReadingReminder(),
    val profile: ReaderProfile = ReaderProfile(),
)
