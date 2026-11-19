package com.nawin.booknook.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.nawin.booknook.domain.model.ShelfTheme

/**
 * The three colours that define a shelf theme in the design: its main colour,
 * a soft tint for chips and highlights, and the page background.
 */
data class ShelfPalette(val primary: Color, val tint: Color, val background: Color)

/** Palettes from the design; [ShelfTheme.STANDARD] is the vanilla look. */
val ShelfTheme.palette: ShelfPalette
    get() = when (this) {
        ShelfTheme.STANDARD -> ShelfPalette(VanillaColors.primary, VanillaColors.highlight, VanillaColors.background)
        ShelfTheme.ROMANTASY -> ShelfPalette(Color(0xFFB4668E), Color(0xFFF4D6E5), Color(0xFFFCF2F7))
        ShelfTheme.WITCHY -> ShelfPalette(Color(0xFF5F5E3C), Color(0xFFD8D2AE), Color(0xFFF1EFE3))
        ShelfTheme.COWBOY -> ShelfPalette(Color(0xFF986741), Color(0xFFE9D3B5), Color(0xFFF8F0E4))
        ShelfTheme.ROMANCE -> ShelfPalette(Color(0xFFC1546C), Color(0xFFF7D0D7), Color(0xFFFDF2F3))
        ShelfTheme.THRILLER -> ShelfPalette(Color(0xFF3D4551), Color(0xFFC8CDD5), Color(0xFFEDEFF2))
    }

/**
 * Full set of library tokens for this theme. Ink, stamps, washi and the gold
 * accent are shared by every theme (they are the "library" itself); the main
 * colour, its tints, highlights and the background change.
 */
fun ShelfTheme.libraryColors(): LibraryColors {
    if (this == ShelfTheme.STANDARD) return VanillaColors
    val (primary, tint, background) = palette
    return VanillaColors.copy(
        background = background,
        paper = lerp(background, Color.White, PAPER_LIFT),
        primary = primary,
        primaryMid = lerp(primary, Color.White, PRIMARY_MID_TINT),
        primaryLight = lerp(primary, Color.White, PRIMARY_LIGHT_TINT),
        accentLight = tint,
        highlight = tint,
        spineLabel = lerp(background, Color.White, PAPER_LIFT),
    )
}

/** How far towards white the paper is from the background (vanilla: #FFF8E6 → #FFFDF6). */
private const val PAPER_LIFT = 0.6f

/** Tints of the main colour, matched to vanilla's #5F86A6 → #BCD1DE and #E3EDF3. */
private const val PRIMARY_MID_TINT = 0.6f
private const val PRIMARY_LIGHT_TINT = 0.83f
