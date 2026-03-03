package com.nawin.booknook.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ─── Composición local para acceder al tema extendido ───
val LocalBookTheme = staticCompositionLocalOf { BookTheme.BOTANICAL }
val LocalJournalStyle = staticCompositionLocalOf { JournalStyle.DOTTED }
val LocalJournalInkColor = staticCompositionLocalOf { JournalInkColor.DARK }

// ─── Helper para acceder al tema desde cualquier Composable ───
object BookNookThemeTokens {
    val inkColor: Color
        @Composable get() {
            val tokens = currentThemeTokens()
            return when (LocalJournalInkColor.current) {
                JournalInkColor.DARK     -> tokens.inkDark
                JournalInkColor.SAGE     -> tokens.inkSage
                JournalInkColor.ROSE     -> BlossomColors.primary
                JournalInkColor.MIDNIGHT -> MidnightColors.primary
            }
        }

    val paperLines: Color
        @Composable get() = currentThemeTokens().paperLines
}

@Composable
fun currentThemeTokens(): ThemeColorTokens = when (LocalBookTheme.current) {
    BookTheme.BOTANICAL -> BotanicalTokens
    BookTheme.SUMMER    -> SummerTokens
    BookTheme.MIDNIGHT  -> MidnightTokens
    BookTheme.BLOSSOM   -> BlossomTokens
}

private fun buildColorScheme(c: Any, isDark: Boolean): ColorScheme {
    return when (c) {
        is BotanicalColors -> if (isDark) darkColorScheme(
            primary = BotanicalColors.primary,
            background = Color(0xFF0F1A12),
            surface = Color(0xFF171F18)
        ) else lightColorScheme(
            primary             = BotanicalColors.primary,
            onPrimary           = BotanicalColors.onPrimary,
            primaryContainer    = BotanicalColors.primaryContainer,
            onPrimaryContainer  = BotanicalColors.onPrimaryContainer,
            secondary           = BotanicalColors.secondary,
            onSecondary         = BotanicalColors.onSecondary,
            secondaryContainer  = BotanicalColors.secondaryContainer,
            onSecondaryContainer = BotanicalColors.onSecondaryContainer,
            tertiary            = BotanicalColors.tertiary,
            onTertiary          = BotanicalColors.onTertiary,
            tertiaryContainer   = BotanicalColors.tertiaryContainer,
            onTertiaryContainer = BotanicalColors.onTertiaryContainer,
            background          = BotanicalColors.background,
            onBackground        = BotanicalColors.onBackground,
            surface             = BotanicalColors.surface,
            onSurface           = BotanicalColors.onSurface,
            surfaceVariant      = BotanicalColors.surfaceVariant,
            onSurfaceVariant    = BotanicalColors.onSurfaceVariant,
            outline             = BotanicalColors.outline
        )
        else -> lightColorScheme()
    }
}

fun getColorScheme(theme: BookTheme, isDark: Boolean): ColorScheme = when (theme) {
    BookTheme.BOTANICAL -> lightColorScheme(
        primary             = BotanicalColors.primary,
        onPrimary           = BotanicalColors.onPrimary,
        primaryContainer    = BotanicalColors.primaryContainer,
        onPrimaryContainer  = BotanicalColors.onPrimaryContainer,
        secondary           = BotanicalColors.secondary,
        onSecondary         = BotanicalColors.onSecondary,
        secondaryContainer  = BotanicalColors.secondaryContainer,
        onSecondaryContainer = BotanicalColors.onSecondaryContainer,
        tertiary            = BotanicalColors.tertiary,
        onTertiary          = BotanicalColors.onTertiary,
        tertiaryContainer   = BotanicalColors.tertiaryContainer,
        onTertiaryContainer = BotanicalColors.onTertiaryContainer,
        background          = BotanicalColors.background,
        onBackground        = BotanicalColors.onBackground,
        surface             = BotanicalColors.surface,
        onSurface           = BotanicalColors.onSurface,
        surfaceVariant      = BotanicalColors.surfaceVariant,
        onSurfaceVariant    = BotanicalColors.onSurfaceVariant,
        outline             = BotanicalColors.outline
    )
    BookTheme.SUMMER -> lightColorScheme(
        primary             = SummerColors.primary,
        onPrimary           = SummerColors.onPrimary,
        primaryContainer    = SummerColors.primaryContainer,
        onPrimaryContainer  = SummerColors.onPrimaryContainer,
        secondary           = SummerColors.secondary,
        onSecondary         = SummerColors.onSecondary,
        secondaryContainer  = SummerColors.secondaryContainer,
        onSecondaryContainer = SummerColors.onSecondaryContainer,
        tertiary            = SummerColors.tertiary,
        onTertiary          = SummerColors.onTertiary,
        tertiaryContainer   = SummerColors.tertiaryContainer,
        onTertiaryContainer = SummerColors.onTertiaryContainer,
        background          = SummerColors.background,
        onBackground        = SummerColors.onBackground,
        surface             = SummerColors.surface,
        onSurface           = SummerColors.onSurface,
        surfaceVariant      = SummerColors.surfaceVariant,
        onSurfaceVariant    = SummerColors.onSurfaceVariant,
        outline             = SummerColors.outline
    )
    BookTheme.MIDNIGHT -> darkColorScheme(
        primary             = MidnightColors.primary,
        onPrimary           = MidnightColors.onPrimary,
        primaryContainer    = MidnightColors.primaryContainer,
        onPrimaryContainer  = MidnightColors.onPrimaryContainer,
        secondary           = MidnightColors.secondary,
        onSecondary         = MidnightColors.onSecondary,
        secondaryContainer  = MidnightColors.secondaryContainer,
        onSecondaryContainer = MidnightColors.onSecondaryContainer,
        tertiary            = MidnightColors.tertiary,
        onTertiary          = MidnightColors.onTertiary,
        tertiaryContainer   = MidnightColors.tertiaryContainer,
        onTertiaryContainer = MidnightColors.onTertiaryContainer,
        background          = MidnightColors.background,
        onBackground        = MidnightColors.onBackground,
        surface             = MidnightColors.surface,
        onSurface           = MidnightColors.onSurface,
        surfaceVariant      = MidnightColors.surfaceVariant,
        onSurfaceVariant    = MidnightColors.onSurfaceVariant,
        outline             = MidnightColors.outline
    )
    BookTheme.BLOSSOM -> lightColorScheme(
        primary             = BlossomColors.primary,
        onPrimary           = BlossomColors.onPrimary,
        primaryContainer    = BlossomColors.primaryContainer,
        onPrimaryContainer  = BlossomColors.onPrimaryContainer,
        secondary           = BlossomColors.secondary,
        onSecondary         = BlossomColors.onSecondary,
        secondaryContainer  = BlossomColors.secondaryContainer,
        onSecondaryContainer = BlossomColors.onSecondaryContainer,
        tertiary            = BlossomColors.tertiary,
        onTertiary          = BlossomColors.onTertiary,
        tertiaryContainer   = BlossomColors.tertiaryContainer,
        onTertiaryContainer = BlossomColors.onTertiaryContainer,
        background          = BlossomColors.background,
        onBackground        = BlossomColors.onBackground,
        surface             = BlossomColors.surface,
        onSurface           = BlossomColors.onSurface,
        surfaceVariant      = BlossomColors.surfaceVariant,
        onSurfaceVariant    = BlossomColors.onSurfaceVariant,
        outline             = BlossomColors.outline
    )
}

@Composable
fun BookNookTheme(
    userPreferences: UserPreferences = UserPreferences(),
    content: @Composable () -> Unit
) {
    val colorScheme = getColorScheme(userPreferences.theme, isSystemInDarkTheme())

    CompositionLocalProvider(
        LocalBookTheme provides userPreferences.theme,
        LocalJournalStyle provides userPreferences.journalStyle,
        LocalJournalInkColor provides userPreferences.journalInkColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = BookNookTypography,
            content     = content
        )
    }
}