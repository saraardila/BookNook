package com.nawin.booknook.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
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

/*
 * Design system of the "personal library" redesign.
 *
 * Every visual value used by the new screens lives here: colours, type,
 * shapes and dimensions. Screens and components read them through
 * [LibraryTheme] and never declare their own literals, so a palette or a
 * spacing change is made in one place.
 *
 * Compose keeps these tokens in Kotlin rather than in res/values: Compose
 * cannot read colour or dimension resources without a Context lookup on every
 * recomposition, and Kotlin tokens move to Compose Multiplatform unchanged.
 * res/values/colors.xml is kept only for the platform (launcher, splash).
 */

// region Fonts

/**
 * Fraunces is a variable font: one file holds every weight, and
 * [FontVariation] selects the instance. The "opsz" axis is the optical size;
 * a high value gives the higher-contrast shapes intended for titles.
 */
private const val FRAUNCES_OPTICAL_SIZE = 72f

private fun fraunces(weight: Int, style: FontStyle = FontStyle.Normal) = Font(
    resId = if (style == FontStyle.Italic) R.font.fraunces_italic else R.font.fraunces,
    weight = FontWeight(weight),
    style = style,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.Setting("opsz", FRAUNCES_OPTICAL_SIZE),
    ),
)

val Fraunces = FontFamily(
    fraunces(FontWeight.Medium.weight),
    fraunces(FontWeight.SemiBold.weight),
    fraunces(FontWeight.Medium.weight, FontStyle.Italic),
)

val Caveat = FontFamily(
    Font(
        resId = R.font.caveat,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.Bold.weight)),
    ),
)

// endregion

// region Colours

/** Colour roles of one palette. Names describe the role, never the hue. */
@Immutable
data class LibraryColors(
    val background: Color,
    val paper: Color,
    val ink: Color,
    val inkMuted: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryMid: Color,
    val primaryLight: Color,
    val accent: Color,
    val accentDeep: Color,
    val accentLight: Color,
    /** Ink stamps and the wax seal. */
    val stamp: Color,
    /** The highlighted "Log" tab and other warm highlights. */
    val highlight: Color,
    val neutralWarm: Color,
    val washi: Color,
    val blush: Color,
    /** Card and control outlines. */
    val outline: Color,
    /** The flat shadow drawn under cards and buttons. */
    val shadow: Color,
    /** Spine labels are always white paper, whatever the palette. */
    val spineLabel: Color,
    val spineLabelInk: Color,
    /** The white border of a polaroid photo. */
    val photoFrame: Color,
)

val VanillaColors = LibraryColors(
    background = Color(0xFFFFF8E6),
    paper = Color(0xFFFFFDF6),
    ink = Color(0xFF5E442E),
    inkMuted = Color(0xFF8C7560),
    primary = Color(0xFF5F86A6),
    onPrimary = Color(0xFFFFF9F4),
    primaryMid = Color(0xFFBCD1DE),
    primaryLight = Color(0xFFE3EDF3),
    accent = Color(0xFFE9CF7A),
    accentDeep = Color(0xFF8E6F2E),
    accentLight = Color(0xFFFFEFB9),
    stamp = Color(0xFF7A4E2D),
    highlight = Color(0xFFFFEFB9),
    neutralWarm = Color(0xFFF2E2C4),
    washi = Color(0xFFA87B56),
    blush = Color(0xFFEDBFB8),
    outline = Color(0x665E442E),
    shadow = Color(0x265E442E),
    spineLabel = Color(0xFFFFFDF6),
    spineLabelInk = Color(0xFF3B2A2E),
    photoFrame = Color(0xFFFFFFFF),
)

// endregion

// region Typography

/** Text styles. [hand] (Caveat) is reserved for short handwritten touches. */
@Immutable
data class LibraryTypography(
    val title: TextStyle,
    /** Between [title] and [titleSmall]: a book title on its own page, a big page number. */
    val heading: TextStyle,
    val titleSmall: TextStyle,
    val emphasis: TextStyle,
    val hand: TextStyle,
    val handSmall: TextStyle,
    val label: TextStyle,
    val navLabel: TextStyle,
    val body: TextStyle,
    /** Small help text under a control. */
    val caption: TextStyle,
    val button: TextStyle,
    val chip: TextStyle,
)

val DefaultLibraryTypography = LibraryTypography(
    title = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 36.sp, letterSpacing = (-0.3).sp),
    heading = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 28.sp),
    titleSmall = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 20.sp),
    emphasis = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 28.sp),
    hand = TextStyle(fontFamily = Caveat, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 22.sp),
    handSmall = TextStyle(fontFamily = Caveat, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 18.sp),
    label = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, letterSpacing = 1.5.sp),
    navLabel = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 10.sp),
    body = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    caption = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 17.sp),
    button = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    chip = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
)

// endregion

// region Shapes and dimensions

/** Everything is rounded; [arch] is a cottage-window arch (round top, soft bottom). */
@Immutable
object LibraryShapes {
    val card = RoundedCornerShape(LibraryDimens.radiusCard)
    val row = RoundedCornerShape(LibraryDimens.radiusRow)
    val label = RoundedCornerShape(LibraryDimens.radiusLabel)
    val labelInner = RoundedCornerShape(LibraryDimens.radiusLabelInner)
    val cover = RoundedCornerShape(LibraryDimens.radiusCover)
    val pill = RoundedCornerShape(percent = 50)
    val quizOption = RoundedCornerShape(LibraryDimens.radiusArchTop, LibraryDimens.radiusArchTop, LibraryDimens.radiusArchBottom, LibraryDimens.radiusArchBottom)
    val quizRow = RoundedCornerShape(LibraryDimens.radiusRowLead, LibraryDimens.radiusArchBottom, LibraryDimens.radiusArchBottom, LibraryDimens.radiusRowLead)
    val arch = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomStartPercent = 10, bottomEndPercent = 10)
    val bottomSheet = RoundedCornerShape(topStart = LibraryDimens.radiusBottomSheet, topEnd = LibraryDimens.radiusBottomSheet)
}

@Immutable
object LibraryDimens {
    // Spacing scale
    val spaceXxs = 2.dp
    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val spaceLg = 16.dp
    val spaceXl = 20.dp
    val screenPadding = 22.dp
    val sectionGap = 28.dp

    // Screen layout
    val headerHeight = 150.dp
    val headerHeightCompact = 112.dp
    /** Outer size of the arch behind the cover on the book page, ring included. */
    val detailArchWidth = 220.dp
    val detailArchHeight = 248.dp
    /** From the top of that arch to the top of the cover. */
    val detailCoverInset = 36.dp
    /** Width of the key column on a catalogue card (YEAR, PAGES…). */
    val catalogueKeyWidth = 74.dp
    val navBarMargin = 16.dp

    // Covers and photos
    val coverLoanWidth = 58.dp
    val coverLoanHeight = 88.dp
    val coverShelfWidth = 90.dp
    val coverShelfHeight = 134.dp
    val coverRowWidth = 50.dp
    val coverRowHeight = 76.dp
    val coverDetailWidth = 124.dp
    val coverDetailHeight = 186.dp
    val coverShareWidth = 140.dp
    val coverShareHeight = 210.dp
    val polaroidBorder = 4.dp
    val polaroidCaption = 12.dp

    // Radii
    val radiusCard = 18.dp
    /** Large sheets such as the reading card. */
    val radiusSheet = 22.dp
    val radiusBottomSheet = 30.dp
    val radiusRow = 16.dp
    val radiusLabel = 12.dp
    val radiusLabelInner = 9.dp
    val radiusCover = 4.dp
    /** Arch top of the test options and the library card portrait. */
    val radiusArchTop = 70.dp
    val radiusArchBottom = 18.dp
    /** Pill-shaped answer rows: round on the icon side. */
    val radiusRowLead = 40.dp

    // Strokes and the flat shadow
    val strokeHairline = 1.dp
    val strokeThin = 1.3.dp
    val stroke = 1.5.dp
    val strokeStamp = 2.5.dp
    val shadowOffset = 4.dp
    val shadowOffsetSmall = 3.dp

    // Controls
    val touchTarget = 48.dp
    val buttonMinHeight = 52.dp
    val chipMinHeight = 36.dp
    val roundButton = 44.dp
    val pageFieldWidth = 140.dp
    val sheetHandleWidth = 44.dp
    val sheetHandleHeight = 5.dp
    val clearButton = 32.dp
    val navBarHeight = 66.dp
    val navIconContainer = 34.dp
    val navIcon = 20.dp
    val progressHeight = 9.dp
    val progressHeightSmall = 7.dp
    val starSize = 13.dp
    val ratingStar = 30.dp
    val badgeIcon = 16.dp

    // Decorations
    val washiHeight = 18.dp
    val washiWidth = 72.dp
    val laceStep = 20.dp
    val laceScallop = 9.dp
    val laceHole = 1.8.dp
    val shelfHeight = 9.dp
    val screwSize = 6.dp
    val punchHole = 12.dp
    val archRingGap = 6.dp
    val stampSize = 62.dp
    val stampSizeSmall = 46.dp
    val rosetteSize = 52.dp
    val themeMiniDot = 10.dp
    val themeMiniSeal = 22.dp
    val toggleWidth = 46.dp
    val toggleHeight = 28.dp
    val toggleThumb = 21.dp
    val sealSize = 72.dp
    val daisySize = 24.dp
    val sprigSize = 46.dp
    val loaderWidth = 72.dp
    val loaderHeight = 56.dp
    val loaderSpineWidth = 13.dp
    val loaderLift = 10.dp

    // Welcome flow: test options and the library card.
    val quizOptionHeight = 178.dp
    val quizOptionIcon = 64.dp
    val quizOptionIconGlyph = 34.dp
    val quizRowIcon = 50.dp
    val quizRowIconGlyph = 26.dp
    val quizSeal = 34.dp
    val quizProgressSegment = 6.dp
    val cardPortraitWidth = 96.dp
    val cardPortraitHeight = 118.dp
    val cardBarcodeWidth = 150.dp
    val cardBarcodeHeight = 32.dp
    val cardSealSize = 74.dp
    val cardStampSize = 58.dp
    val cardPlaceholderStamp = 86.dp
    val resultArchWidth = 270.dp
    val resultArchHeight = 250.dp
    val resultSpineWidth = 22.dp
    val streakDay = 30.dp
    val streakDayLarge = 34.dp
    val newStampSize = 108.dp
    val streakDayCheck = 14.dp
    val resultThemeMini = 64.dp
    /** Layout size of a share card (9:16, like a story); the screen scales it to fit. */
    val shareCardWidth = 340.dp
    val shareCardHeight = 604.dp
    val shareHeaderHeight = 104.dp
    val shareActionSize = 56.dp
    val homeBlockIcon = 38.dp

    // Reading calendar
    val calendarDayBadge = 18.dp
    val calendarReadDisc = 32.dp
    val calendarLegendDot = 10.dp
    val dragHandleDot = 4.dp
    val dragHandleGap = 3.dp
}

// endregion

// region Theme access

val LocalLibraryColors = staticCompositionLocalOf { VanillaColors }
val LocalLibraryTypography = staticCompositionLocalOf { DefaultLibraryTypography }

/** Reads the current tokens, e.g. `LibraryTheme.colors.primary`. */
object LibraryTheme {
    val colors: LibraryColors
        @Composable @ReadOnlyComposable get() = LocalLibraryColors.current
    val typography: LibraryTypography
        @Composable @ReadOnlyComposable get() = LocalLibraryTypography.current
    val shapes: LibraryShapes get() = LibraryShapes
    val dimens: LibraryDimens get() = LibraryDimens
}

/** Provides the redesign's tokens to [content]. Shelf themes pass their own [colors]. */
@Composable
fun LibraryTheme(
    colors: LibraryColors = VanillaColors,
    typography: LibraryTypography = DefaultLibraryTypography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLibraryColors provides colors,
        LocalLibraryTypography provides typography,
        content = content,
    )
}

// endregion
