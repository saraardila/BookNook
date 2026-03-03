package com.nawin.booknook.presentation.theme

enum class BookTheme {
    BOTANICAL,
    SUMMER,
    MIDNIGHT,
    BLOSSOM
}

enum class JournalStyle {
    PLAIN,
    LINED,
    DOTTED,
    GRID
}

data class UserPreferences(
    val theme: BookTheme = BookTheme.BOTANICAL,
    val journalStyle: JournalStyle = JournalStyle.DOTTED,
    val journalInkColor: JournalInkColor = JournalInkColor.DARK
)

enum class JournalInkColor {
    DARK, SAGE, ROSE, MIDNIGHT
}