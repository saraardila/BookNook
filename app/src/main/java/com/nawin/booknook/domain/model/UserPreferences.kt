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

/** Paper of the journal pages. Stored, but not shown until the journal is redesigned. */
enum class JournalStyle { PLAIN, LINED, DOTTED, GRID }

/** Ink of the journal writing. Stored, but not shown until the journal is redesigned. */
enum class JournalInkColor { DARK, SAGE, ROSE, MIDNIGHT }

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
 */
data class ReaderProfile(
    val name: String = "",
    val memberSince: Long? = null,
)

/** Everything the reader can change in Settings. */
data class UserPreferences(
    val theme: ShelfTheme = ShelfTheme.STANDARD,
    val journalStyle: JournalStyle = JournalStyle.DOTTED,
    val journalInkColor: JournalInkColor = JournalInkColor.DARK,
    val reminder: ReadingReminder = ReadingReminder(),
    val profile: ReaderProfile = ReaderProfile(),
)
