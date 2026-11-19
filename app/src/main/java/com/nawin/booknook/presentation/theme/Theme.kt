package com.nawin.booknook.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Material theme built from the library tokens, so the screens and dialogs not
 * redesigned yet follow the chosen shelf theme too. Removed once nothing uses
 * MaterialTheme colours directly.
 */
@Composable
fun BookNookTheme(colors: LibraryColors, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colors.toMaterialColorScheme(),
        typography = BookNookTypography,
        content = content,
    )
}

/** Maps the library tokens onto Material's roles. */
fun LibraryColors.toMaterialColorScheme(): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryLight,
    onPrimaryContainer = ink,
    secondary = accentDeep,
    onSecondary = onPrimary,
    secondaryContainer = highlight,
    onSecondaryContainer = ink,
    tertiary = stamp,
    onTertiary = onPrimary,
    tertiaryContainer = blush,
    onTertiaryContainer = ink,
    background = background,
    onBackground = ink,
    surface = paper,
    onSurface = ink,
    surfaceVariant = neutralWarm,
    onSurfaceVariant = inkMuted,
    surfaceContainerHigh = paper,
    surfaceContainerHighest = paper,
    outline = outline,
    outlineVariant = outline,
)
