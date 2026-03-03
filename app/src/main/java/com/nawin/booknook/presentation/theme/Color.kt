package com.nawin.booknook.presentation.theme

import androidx.compose.ui.graphics.Color

// ─── BOTANICAL 🌿 ───────────────────────────────
object BotanicalColors {
    val background      = Color(0xFFF5F0E8)
    val surface         = Color(0xFFEDE8DC)
    val surfaceVariant  = Color(0xFFE0D9C8)
    val primary         = Color(0xFF4A7C59)
    val onPrimary       = Color(0xFFFFFFFF)
    val primaryContainer    = Color(0xFFBDD8C4)
    val onPrimaryContainer  = Color(0xFF1A3D28)
    val secondary           = Color(0xFF7A9E7E)
    val onSecondary         = Color(0xFFFFFFFF)
    val secondaryContainer  = Color(0xFFD4E8D4)
    val onSecondaryContainer = Color(0xFF2D4A3E)
    val tertiary            = Color(0xFF8B7355)
    val onTertiary          = Color(0xFFFFFFFF)
    val tertiaryContainer   = Color(0xFFE8DCC8)
    val onTertiaryContainer = Color(0xFF3D2E1A)
    val onBackground        = Color(0xFF1C1B18)
    val onSurface           = Color(0xFF1C1B18)
    val onSurfaceVariant    = Color(0xFF4A4639)
    val outline             = Color(0xFF9E9482)
    // Journal
    val inkDark     = Color(0xFF2C2416)
    val inkSage     = Color(0xFF3D5E44)
    val paperLines  = Color(0xFFCFC8B4)
}

// ─── SUMMER ☀️ ───────────────────────────────────
object SummerColors {
    val background      = Color(0xFFFFFBF0)
    val surface         = Color(0xFFFFF5D6)
    val surfaceVariant  = Color(0xFFFFEDB8)
    val primary         = Color(0xFFE07B39)
    val onPrimary       = Color(0xFFFFFFFF)
    val primaryContainer    = Color(0xFFFFD4A8)
    val onPrimaryContainer  = Color(0xFF5C2800)
    val secondary           = Color(0xFFE8A020)
    val onSecondary         = Color(0xFFFFFFFF)
    val secondaryContainer  = Color(0xFFFFE0A0)
    val onSecondaryContainer = Color(0xFF3D2800)
    val tertiary            = Color(0xFFD45C6A)
    val onTertiary          = Color(0xFFFFFFFF)
    val tertiaryContainer   = Color(0xFFFFBFC4)
    val onTertiaryContainer = Color(0xFF4A1020)
    val onBackground        = Color(0xFF1C1500)
    val onSurface           = Color(0xFF1C1500)
    val onSurfaceVariant    = Color(0xFF4A3C00)
    val outline             = Color(0xFFB8A060)
    val inkDark     = Color(0xFF2C1800)
    val inkSage     = Color(0xFF5C4400)
    val paperLines  = Color(0xFFE8D090)
}

// ─── MIDNIGHT 🌙 ──────────────────────────────────
object MidnightColors {
    val background      = Color(0xFF0F1420)
    val surface         = Color(0xFF171E2E)
    val surfaceVariant  = Color(0xFF1E2840)
    val primary         = Color(0xFFD4A843)
    val onPrimary       = Color(0xFF1A0E00)
    val primaryContainer    = Color(0xFF3D2E08)
    val onPrimaryContainer  = Color(0xFFFFD97A)
    val secondary           = Color(0xFF7B9FCC)
    val onSecondary         = Color(0xFF0A1830)
    val secondaryContainer  = Color(0xFF1A3050)
    val onSecondaryContainer = Color(0xFFB8D4F0)
    val tertiary            = Color(0xFF9B7FCC)
    val onTertiary          = Color(0xFF1A0A30)
    val tertiaryContainer   = Color(0xFF2A1A48)
    val onTertiaryContainer = Color(0xFFD4B8F0)
    val onBackground        = Color(0xFFE8E0D0)
    val onSurface           = Color(0xFFE8E0D0)
    val onSurfaceVariant    = Color(0xFFB0A890)
    val outline             = Color(0xFF3A4060)
    val inkDark     = Color(0xFFE8D8A0)
    val inkSage     = Color(0xFF7BB88C)
    val paperLines  = Color(0xFF2A3050)
}

// ─── BLOSSOM 🌸 ───────────────────────────────────
object BlossomColors {
    val background      = Color(0xFFFDF5F7)
    val surface         = Color(0xFFF8ECF0)
    val surfaceVariant  = Color(0xFFF0E0E8)
    val primary         = Color(0xFFB85C78)
    val onPrimary       = Color(0xFFFFFFFF)
    val primaryContainer    = Color(0xFFFFD4DF)
    val onPrimaryContainer  = Color(0xFF4A0820)
    val secondary           = Color(0xFF7A9E7E)
    val onSecondary         = Color(0xFFFFFFFF)
    val secondaryContainer  = Color(0xFFD4E8D4)
    val onSecondaryContainer = Color(0xFF1A3D28)
    val tertiary            = Color(0xFFB89860)
    val onTertiary          = Color(0xFFFFFFFF)
    val tertiaryContainer   = Color(0xFFF0E0B8)
    val onTertiaryContainer = Color(0xFF3D2800)
    val onBackground        = Color(0xFF201018)
    val onSurface           = Color(0xFF201018)
    val onSurfaceVariant    = Color(0xFF4A2838)
    val outline             = Color(0xFFB89098)
    val inkDark     = Color(0xFF2C1018)
    val inkSage     = Color(0xFF3D5E44)
    val paperLines  = Color(0xFFE8C8D4)
}

// ─── Data class para acceso dinámico ───────────────
data class ThemeColorTokens(
    val inkDark: Color,
    val inkSage: Color,
    val paperLines: Color
)

val BotanicalTokens = ThemeColorTokens(
    inkDark    = BotanicalColors.inkDark,
    inkSage    = BotanicalColors.inkSage,
    paperLines = BotanicalColors.paperLines
)

val SummerTokens = ThemeColorTokens(
    inkDark    = SummerColors.inkDark,
    inkSage    = SummerColors.inkSage,
    paperLines = SummerColors.paperLines
)

val MidnightTokens = ThemeColorTokens(
    inkDark    = MidnightColors.inkDark,
    inkSage    = MidnightColors.inkSage,
    paperLines = MidnightColors.paperLines
)

val BlossomTokens = ThemeColorTokens(
    inkDark    = BlossomColors.inkDark,
    inkSage    = BlossomColors.inkSage,
    paperLines = BlossomColors.paperLines
)