package com.nawin.booknook.presentation.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.presentation.components.library.InkStamp
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.Polaroid
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.components.library.WashiTape
import com.nawin.booknook.presentation.components.library.WaxSeal
import com.nawin.booknook.presentation.components.library.drawFlatShadow
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.theme.code
import com.nawin.booknook.presentation.theme.nameRes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * What the library card shows. All null before the reader has one (the welcome
 * page shows the empty card with placeholders). [year] and [shelfCode] are
 * printed under the barcode and on the ink stamp.
 */
data class LibraryCardData(
    val holder: String? = null,
    val shelfName: String? = null,
    val shelfCode: String? = null,
    val since: String? = null,
    val year: String? = null,
) {
    val issued: Boolean get() = holder != null
}

/**
 * The card printed for [profile]: holder, favourite shelf and its code, and the
 * month and year she joined in the app's locale (today if not recorded yet).
 */
@Composable
fun libraryCardData(profile: ReaderProfile): LibraryCardData {
    val locale = LocalConfiguration.current.locales[0]
    val shelf = profile.favouriteShelf
    val joined = remember(profile.memberSince) {
        Instant.ofEpochMilli(profile.memberSince ?: System.currentTimeMillis()).atZone(ZoneId.systemDefault())
    }
    return LibraryCardData(
        holder = profile.name,
        shelfName = shelf?.let { stringResource(it.nameRes) },
        shelfCode = shelf?.code,
        since = joined.format(DateTimeFormatter.ofPattern(MONTH_YEAR_PATTERN, locale)),
        year = joined.year.toString(),
    )
}

/** Short month and year, e.g. "nov 2026"; LLL is the stand-alone month form. */
private const val MONTH_YEAR_PATTERN = "LLL yyyy"

/**
 * The reader's library card: a coloured header with lace, the portrait (the
 * initial, or a "?" polaroid while empty), the holder's fields and, once issued,
 * a barcode, an ink stamp and a wax seal.
 */
@Composable
fun LibraryCard(data: LibraryCardData, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val shape = LibraryShapes.card
    Box(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .drawBehind { drawFlatShadow(colors.shadow, CornerRadius(LibraryDimens.radiusCard.toPx())) }
                .clip(shape)
                .background(colors.paper, shape)
                .border(LibraryDimens.stroke, colors.outline, shape),
        ) {
            CardHeader()
            Row(
                Modifier.padding(
                    start = LibraryDimens.spaceLg,
                    end = LibraryDimens.spaceLg,
                    top = LibraryDimens.spaceLg + LibraryDimens.laceScallop,
                    bottom = LibraryDimens.spaceMd,
                ),
                horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceLg),
            ) {
                if (data.issued) Portrait(data.holder.orEmpty()) else EmptyPortrait()
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm)) {
                    CardField(
                        stringResource(R.string.card_holder),
                        data.holder ?: stringResource(R.string.card_holder_unknown),
                        emphasised = data.issued,
                    )
                    CardField(stringResource(R.string.card_favourite_shelf), data.shelfName ?: stringResource(R.string.card_shelf_unknown))
                    CardField(stringResource(R.string.card_since), data.since ?: stringResource(R.string.card_empty_value))
                }
            }
            if (data.issued) {
                CardFooter(data)
            }
        }
        WashiTape(
            Modifier.align(Alignment.TopStart).offset(x = LibraryDimens.sectionGap, y = -LibraryDimens.spaceSm),
            width = LibraryDimens.washiWidth + LibraryDimens.spaceSm,
        )
        if (data.issued) {
            WaxSeal(
                Modifier.align(Alignment.BottomEnd).offset(x = LibraryDimens.spaceSm, y = LibraryDimens.spaceXl).rotate(SEAL_ROTATION),
                size = LibraryDimens.cardSealSize,
                color = colors.stamp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(LibraryDimens.navIcon))
                    Text(
                        stringResource(R.string.card_seal),
                        style = type.label.copy(fontSize = type.navLabel.fontSize * SEAL_TEXT_SCALE, lineHeight = type.navLabel.fontSize),
                        color = colors.onPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            PlaceholderStamp(Modifier.align(Alignment.BottomEnd).offset(x = LibraryDimens.spaceMd, y = LibraryDimens.sectionGap + LibraryDimens.spaceSm))
        }
    }
}

@Composable
private fun CardHeader() {
    val colors = LibraryTheme.colors
    Box {
        LaceHeader(
            height = LibraryDimens.headerHeightCompact / 2 + LibraryDimens.spaceSm,
            color = colors.primary,
            holeColor = colors.paper,
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = LibraryDimens.spaceLg, vertical = LibraryDimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.card_title), style = LibraryTheme.typography.label, color = colors.onPrimary)
                Text(stringResource(R.string.app_name), style = LibraryTheme.typography.titleSmall, color = colors.onPrimary)
            }
            Sprig(size = LibraryDimens.navIconContainer, stem = colors.onPrimary, leaf = colors.primaryMid)
        }
    }
}

@Composable
private fun Portrait(holder: String) {
    val colors = LibraryTheme.colors
    val shape = LibraryShapes.quizOption
    Box(
        Modifier
            .size(LibraryDimens.cardPortraitWidth, LibraryDimens.cardPortraitHeight)
            .background(colors.primaryMid, shape)
            .border(LibraryDimens.stroke, colors.outline, shape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            holder.firstOrNull()?.uppercase(LocalConfiguration.current.locales[0]).orEmpty(),
            style = LibraryTheme.typography.title.copy(fontSize = LibraryTheme.typography.title.fontSize * PORTRAIT_INITIAL_SCALE),
            color = colors.paper,
        )
    }
}

/** A blank polaroid with a dashed frame and a question mark: "who are you?". */
@Composable
private fun EmptyPortrait() {
    val colors = LibraryTheme.colors
    Polaroid(Modifier.clearAndSetSemantics {}) {
        Box(
            Modifier
                .size(LibraryDimens.cardPortraitWidth - LibraryDimens.sectionGap, LibraryDimens.cardPortraitHeight - LibraryDimens.sectionGap - LibraryDimens.spaceXs)
                .dashedBorder(colors.outline),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.card_unknown_initial), style = LibraryTheme.typography.hand.copy(fontSize = LibraryTheme.typography.title.fontSize), color = colors.inkMuted)
        }
    }
}

@Composable
private fun CardField(label: String, value: String, emphasised: Boolean = false) {
    val type = LibraryTheme.typography
    Column {
        Text(label, style = type.label, color = LibraryTheme.colors.inkMuted)
        Text(
            value,
            style = if (emphasised) type.heading else type.titleSmall,
            color = LibraryTheme.colors.ink,
            maxLines = 1,
        )
    }
}

@Composable
private fun CardFooter(data: LibraryCardData) {
    val colors = LibraryTheme.colors
    val reference = listOfNotNull(data.year, data.shelfCode).joinToString(CARD_REFERENCE_SEPARATOR)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = LibraryDimens.spaceLg, end = LibraryDimens.spaceLg, bottom = LibraryDimens.spaceLg)
            .dashedTopBorder(colors.outline)
            .padding(top = LibraryDimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).clearAndSetSemantics {}) {
            Barcode(seed = data.holder.orEmpty() + reference)
            Text(reference, style = LibraryTheme.typography.label, color = colors.inkMuted)
        }
        InkStamp(
            top = stringResource(R.string.card_stamp_reader),
            middle = data.year.orEmpty(),
            bottom = data.shelfCode.orEmpty(),
            size = LibraryDimens.cardStampSize,
            color = colors.stamp,
            modifier = Modifier.padding(end = LibraryDimens.sectionGap + LibraryDimens.spaceXl),
        )
    }
}

/**
 * Decorative barcode. The bar widths come from [seed] so each card looks
 * different but always the same; it encodes nothing.
 */
@Composable
private fun Barcode(seed: String) {
    val ink = LibraryTheme.colors.ink
    val widths = remember(seed) { barcodeWidths(seed) }
    Canvas(Modifier.size(LibraryDimens.cardBarcodeWidth, LibraryDimens.cardBarcodeHeight)) {
        val unit = size.width / widths.sum()
        var x = 0f
        widths.forEachIndexed { index, width ->
            if (index % 2 == 0) drawRect(ink, topLeft = Offset(x, 0f), size = Size(width * unit, size.height))
            x += width * unit
        }
    }
}

private fun barcodeWidths(seed: String): List<Int> {
    var state = seed.hashCode()
    return List(BARCODE_BARS) {
        state = state * BARCODE_MULTIPLIER + BARCODE_INCREMENT
        1 + (state ushr BARCODE_SHIFT) % BARCODE_MAX_WIDTH
    }
}

/** The dashed circle where the first seal will go. */
@Composable
private fun PlaceholderStamp(modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    Box(
        modifier
            .rotate(PLACEHOLDER_ROTATION)
            .size(LibraryDimens.cardPlaceholderStamp)
            .background(colors.background, LibraryShapes.pill)
            .drawBehind {
                drawCircle(
                    colors.stamp,
                    radius = size.minDimension / 2 - LibraryDimens.strokeStamp.toPx(),
                    style = Stroke(LibraryDimens.strokeStamp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF))),
                )
            }
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(R.string.card_seal_placeholder),
            style = LibraryTheme.typography.label,
            color = colors.stamp,
            textAlign = TextAlign.Center,
        )
    }
}

private fun Modifier.dashedBorder(color: Color): Modifier = drawBehind {
    drawRect(color, style = Stroke(LibraryDimens.stroke.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF))))
}

private fun Modifier.dashedTopBorder(color: Color): Modifier = drawBehind {
    drawLine(
        color,
        start = Offset.Zero,
        end = Offset(size.width, 0f),
        strokeWidth = LibraryDimens.stroke.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF)),
    )
}

private const val SEAL_ROTATION = -12f
private const val PLACEHOLDER_ROTATION = -12f
private const val SEAL_TEXT_SCALE = 0.75f
private const val PORTRAIT_INITIAL_SCALE = 1.3f
private const val DASH_ON = 10f
private const val DASH_OFF = 7f
private const val CARD_REFERENCE_SEPARATOR = " · "
private const val BARCODE_BARS = 41
private const val BARCODE_MAX_WIDTH = 4
private const val BARCODE_MULTIPLIER = 1_103_515_245
private const val BARCODE_INCREMENT = 12_345
private const val BARCODE_SHIFT = 16
