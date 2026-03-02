package com.nawin.booknook.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CoffeeBrown,
    onPrimary = CreamWhite,
    primaryContainer = WarmParchment,
    onPrimaryContainer = DarkBrown,
    secondary = SageGreen,
    onSecondary = CreamWhite,
    secondaryContainer = Color(0xFFD4E8D4),
    onSecondaryContainer = DeepForest,
    tertiary = DustyRose,
    onTertiary = CreamWhite,
    tertiaryContainer = BlushPink,
    onTertiaryContainer = DeepRose,
    background = CreamWhite,
    onBackground = TextPrimary,
    surface = SoftLinen,
    onSurface = TextPrimary,
    surfaceVariant = WarmParchment,
    onSurfaceVariant = TextSecondary,
    outline = TextHint
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmParchment,
    onPrimary = DarkBrown,
    primaryContainer = CoffeeBrown,
    onPrimaryContainer = CreamWhite,
    secondary = SageGreen,
    onSecondary = DeepForest,
    background = Color(0xFF1A120B),
    onBackground = CreamWhite,
    surface = Color(0xFF2C1810),
    onSurface = WarmParchment,
    surfaceVariant = Color(0xFF3D2314),
    onSurfaceVariant = WarmParchment
)

@Composable
fun BookNookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = BookNookTypography,
        content = content
    )
}
