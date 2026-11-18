package com.nawin.booknook.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawin.booknook.R

// Design tokens for the "tu biblioteca personal" redesign (vainilla).
// Nothing uses them yet: screens move over one by one in later steps.

// Fraunces and Caveat are variable fonts: one file holds every weight, and
// FontVariation picks the instance. "opsz" is Fraunces' optical size axis:
// a high value draws the thinner, more contrasted shapes meant for titles.
private fun fraunces(weight: Int, style: FontStyle = FontStyle.Normal) = Font(
    resId = if (style == FontStyle.Italic) R.font.fraunces_italic else R.font.fraunces,
    weight = FontWeight(weight),
    style = style,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.Setting("opsz", 72f),
    ),
)

val Fraunces = FontFamily(
    fraunces(500),
    fraunces(600),
    fraunces(500, FontStyle.Italic),
)

val Caveat = FontFamily(
    Font(
        resId = R.font.caveat,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

/** Colours of one palette. Names follow the role in the design, not the hue. */
@Immutable
data class BibliotecaColors(
    val background: Color,   // page
    val paper: Color,        // cards
    val ink: Color,          // text
    val muted: Color,        // secondary text
    val primary: Color,      // buttons, active tab, header of the library card
    val primaryMid: Color,   // photo placeholder, soft fills
    val primaryLight: Color, // headers with lace, chips
    val accent: Color,
    val accentDeep: Color,   // progress bars, shelf
    val accentLight: Color,
    val stamp: Color,        // ink stamps and wax seal
    val butter: Color,       // "Anotar" button, highlights
    val sky: Color,
    val washi: Color,        // washi tape
    val rose: Color,
    val line: Color,         // card outlines
    val shadow: Color,       // the flat 4dp shadow under cards
    val onPrimary: Color,
)

val Vainilla = BibliotecaColors(
    background = Color(0xFFFFF8E6),
    paper = Color(0xFFFFFDF6),
    ink = Color(0xFF5E442E),
    muted = Color(0xFF8C7560),
    primary = Color(0xFF5F86A6),
    primaryMid = Color(0xFFBCD1DE),
    primaryLight = Color(0xFFE3EDF3),
    accent = Color(0xFFE9CF7A),
    accentDeep = Color(0xFF8E6F2E),
    accentLight = Color(0xFFFFEFB9),
    stamp = Color(0xFF7A4E2D),
    butter = Color(0xFFFFEFB9),
    sky = Color(0xFFF2E2C4),
    washi = Color(0xFFA87B56),
    rose = Color(0xFFEDBFB8),
    line = Color(0x665E442E),   // ink at 40 %
    shadow = Color(0x265E442E), // ink at 15 %
    onPrimary = Color(0xFFFFF9F4),
)

/** Text styles of the redesign. Caveat is only for short handwritten bits. */
@Immutable
data class BibliotecaType(
    val title: TextStyle,     // "Mi biblioteca", screen titles
    val titleSmall: TextStyle,// book titles in cards and lists
    val italic: TextStyle,    // the stressed word in a question ("leer")
    val hand: TextStyle,      // greetings, notes, captions
    val label: TextStyle,     // "FICHA DE PRÉSTAMO · Nº 14"
    val body: TextStyle,
    val button: TextStyle,
)

val BibliotecaTypography = BibliotecaType(
    title = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight(600), fontSize = 34.sp, lineHeight = 36.sp, letterSpacing = (-0.3).sp),
    titleSmall = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight(600), fontSize = 17.sp, lineHeight = 20.sp),
    italic = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight(500), fontStyle = FontStyle.Italic, fontSize = 28.sp),
    hand = TextStyle(fontFamily = Caveat, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 22.sp),
    label = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, letterSpacing = 1.5.sp),
    body = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    button = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 16.sp),
)

/** Corner radii. Everything is rounded; the arch is a pill on top only. */
object BibliotecaShapes {
    val card = RoundedCornerShape(18.dp)
    val row = RoundedCornerShape(16.dp)
    val pill = RoundedCornerShape(percent = 50)
    val chip = RoundedCornerShape(12.dp)
    val cover = RoundedCornerShape(4.dp)
    val arch = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomStartPercent = 10, bottomEndPercent = 10)
}

val LocalBibliotecaColors = staticCompositionLocalOf { Vainilla }
val LocalBibliotecaType = staticCompositionLocalOf { BibliotecaTypography }

/** Read the tokens from any composable: `Biblioteca.colors.primary`. */
object Biblioteca {
    val colors: BibliotecaColors
        @Composable get() = LocalBibliotecaColors.current
    val type: BibliotecaType
        @Composable get() = LocalBibliotecaType.current
}

@Composable
fun BibliotecaTheme(
    colors: BibliotecaColors = Vainilla,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalBibliotecaColors provides colors,
        LocalBibliotecaType provides BibliotecaTypography,
        content = content,
    )
}
