package com.nawin.booknook.presentation.components.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle as DateTextStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Round ink stamp with a double ring and three lines of text, e.g.
 * READ / 02 / OCT on the reading card or RETURN / 30 / NOV on the loan card.
 * The text is passed in already localised. [struck] crosses out the middle
 * line (used for a book the reader gave up on).
 */
@Composable
fun InkStamp(
    top: String,
    middle: String,
    bottom: String,
    modifier: Modifier = Modifier,
    size: Dp = LibraryDimens.stampSize,
    color: Color = LibraryTheme.colors.stamp,
    rotation: Float = STAMP_DEFAULT_ROTATION,
    background: Color = Color.Transparent,
    struck: Boolean = false,
) {
    val type = LibraryTheme.typography
    // Text scales with the stamp so any size keeps the proportions of the artwork.
    val scale = size.value
    Box(
        modifier
            .size(size)
            .rotate(rotation)
            .alpha(STAMP_INK_OPACITY)
            .semantics { contentDescription = "$top $middle $bottom" }
            .drawBehind {
                val outer = this.size.minDimension / 2 - LibraryDimens.strokeStamp.toPx() / 2
                drawCircle(background)
                drawCircle(color, radius = outer, style = Stroke(LibraryDimens.strokeStamp.toPx()))
                drawCircle(color, radius = outer - STAMP_RING_GAP.toPx(), style = Stroke(LibraryDimens.strokeThin.toPx()))
            },
        contentAlignment = Alignment.Center,
    ) {
        // Outer lines sit where the circle is narrower, so they get less width and
        // shrink to fit: long words ("EMPEZADO", "ABANDONADO") stay inside the ring.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StampLine(top, type.label.copy(letterSpacing = 1.sp, color = color), maxSize = (scale * 0.13f).sp, width = size * STAMP_OUTER_LINE_WIDTH)
            Text(
                middle,
                style = type.titleSmall.copy(
                    fontSize = (scale * 0.3f).sp,
                    lineHeight = (scale * 0.32f).sp,
                    textDecoration = if (struck) TextDecoration.LineThrough else null,
                ),
                color = color,
                maxLines = 1,
            )
            StampLine(bottom, type.label.copy(letterSpacing = 1.sp, color = color), maxSize = (scale * 0.12f).sp, width = size * STAMP_OUTER_LINE_WIDTH)
        }
    }
}

/** One outer line of a stamp, auto-sized down from [maxSize] to fit [width] on a single line. */
@Composable
private fun StampLine(text: String, style: TextStyle, maxSize: TextUnit, width: Dp) {
    BasicText(
        text = text,
        style = style.copy(textAlign = TextAlign.Center),
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(minFontSize = STAMP_MIN_TEXT, maxFontSize = maxSize),
        modifier = Modifier.width(width),
    )
}

/**
 * [InkStamp] showing a day and short month under [label] (e.g. READ / 28 / OCT),
 * with the month name formatted for the current locale.
 */
@Composable
fun DateStamp(
    label: String,
    epochMillis: Long,
    modifier: Modifier = Modifier,
    size: Dp = LibraryDimens.stampSize,
    rotation: Float = STAMP_DEFAULT_ROTATION,
    background: Color = Color.Transparent,
) {
    val locale = LocalConfiguration.current.locales[0]
    val date = remember(epochMillis) { Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
    InkStamp(
        top = label,
        middle = date.dayOfMonth.toString().padStart(DAY_DIGITS, '0'),
        bottom = date.month.getDisplayName(DateTextStyle.SHORT, locale).uppercase(locale).trimEnd('.'),
        modifier = modifier,
        size = size,
        rotation = rotation,
        background = background,
    )
}

private const val DAY_DIGITS = 2
private val STAMP_MIN_TEXT = 5.sp
private const val STAMP_OUTER_LINE_WIDTH = 0.62f
private const val STAMP_DEFAULT_ROTATION = -8f
private const val STAMP_INK_OPACITY = 0.92f
private val STAMP_RING_GAP = 4.dp

/**
 * Closed scalloped outline around a circle, shared by [Rosette] and [WaxSeal].
 * Each bump is a half circle centred on the base circle and facing outwards.
 */
internal fun scallopPath(center: Offset, radius: Float, bumps: Int, depth: Float): Path {
    val path = Path()
    val base = radius * (1 - depth)
    val bumpRadius = (2 * base * sin(PI / bumps)).toFloat() / 2 * SCALLOP_OVERLAP
    for (i in 0 until bumps) {
        val angle = (2 * PI * i / bumps - PI / 2).toFloat()
        val bumpCenter = Offset(center.x + base * cos(angle), center.y + base * sin(angle))
        val startDegrees = Math.toDegrees(angle.toDouble()).toFloat() - 90f
        path.arcTo(Rect(bumpCenter, bumpRadius), startDegrees, 180f, forceMoveTo = i == 0)
    }
    path.close()
    return path
}

private const val SCALLOP_OVERLAP = 1.12f

/**
 * Rosette badge for a shelf licence. [dashed] draws one not won yet;
 * [ring] adds the dotted inner ring of a licence already earned.
 */
@Composable
fun Rosette(
    modifier: Modifier = Modifier,
    size: Dp = LibraryDimens.rosetteSize,
    fill: Color = LibraryTheme.colors.highlight,
    rotation: Float = 0f,
    dashed: Boolean = false,
    ring: Color? = null,
    content: @Composable () -> Unit = {},
) {
    val outline = LibraryTheme.colors.ink
    Box(
        modifier
            .size(size)
            .rotate(rotation)
            .drawBehind {
                val path = scallopPath(center, this.size.minDimension / 2, ROSETTE_BUMPS, ROSETTE_DEPTH)
                if (!dashed) drawPath(path, fill)
                val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.5.dp.toPx()))
                drawPath(path, outline, style = Stroke(LibraryDimens.stroke.toPx(), pathEffect = if (dashed) dash else null))
                if (ring != null) {
                    drawCircle(
                        ring,
                        radius = this.size.minDimension * ROSETTE_RING_RATIO,
                        style = Stroke(LibraryDimens.strokeHairline.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx()))),
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) { content() }
}

private const val ROSETTE_BUMPS = 16
private const val ROSETTE_DEPTH = 0.09f
private const val ROSETTE_RING_RATIO = 0.31f

/** Wax seal: a deep-coloured disc with a wobbly edge and a light inner ring. */
@Composable
fun WaxSeal(
    modifier: Modifier = Modifier,
    size: Dp = LibraryDimens.sealSize,
    color: Color = LibraryTheme.colors.stamp,
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier
            .size(size)
            .drawBehind {
                drawPath(scallopPath(center, this.size.minDimension / 2, SEAL_BUMPS, SEAL_DEPTH), color)
                drawCircle(
                    Color.White.copy(alpha = SEAL_RING_OPACITY),
                    radius = this.size.minDimension * SEAL_RING_RATIO,
                    style = Stroke(LibraryDimens.stroke.toPx()),
                )
            },
        contentAlignment = Alignment.Center,
    ) { content() }
}

private const val SEAL_BUMPS = 22
private const val SEAL_DEPTH = 0.05f
private const val SEAL_RING_OPACITY = 0.35f
private const val SEAL_RING_RATIO = 0.33f
