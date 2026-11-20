package com.nawin.booknook.presentation.components.library

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme

/*
 * Hand-drawn touches of the redesign. They are plain Canvas drawing (no
 * bitmaps, no blur), so they adapt to every palette and are cheap to render
 * inside scrolling lists. Ratios inside a drawing are expressed in a 24- or
 * 48-unit design grid, matching the source artwork.
 */

/**
 * Header band whose bottom edge is a row of scallops with small holes, like a
 * paper doily. The scallops are drawn below the band's bounds, so leave
 * [LibraryDimens.laceScallop] of space under it.
 */
@Composable
fun LaceHeader(
    height: Dp,
    modifier: Modifier = Modifier,
    color: Color = LibraryTheme.colors.primaryLight,
    holeColor: Color = LibraryTheme.colors.background,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                drawRect(color)
                val step = LibraryDimens.laceStep.toPx()
                val scallop = LibraryDimens.laceScallop.toPx()
                val hole = LibraryDimens.laceHole.toPx()
                var x = step / 2
                while (x < size.width + step) {
                    drawCircle(color, radius = scallop, center = Offset(x, size.height))
                    drawCircle(holeColor, radius = hole, center = Offset(x, size.height - scallop / 3))
                    x += step
                }
            },
    )
}

enum class WashiPattern { Dots, Stripes, Plain }

/** A strip of washi tape with torn ends. Lay it over a card's edge with an offset. */
@Composable
fun WashiTape(
    modifier: Modifier = Modifier,
    width: Dp = LibraryDimens.washiWidth,
    rotation: Float = WASHI_DEFAULT_ROTATION,
    color: Color = LibraryTheme.colors.washi,
    pattern: WashiPattern = WashiPattern.Dots,
) {
    Canvas(
        modifier
            .width(width)
            .height(LibraryDimens.washiHeight)
            .rotate(rotation),
    ) {
        clipPath(tornStripPath(size)) {
            drawRect(color.copy(alpha = WASHI_OPACITY))
            drawWashiPattern(pattern, Color.White.copy(alpha = WASHI_PATTERN_OPACITY))
        }
    }
}

private const val WASHI_DEFAULT_ROTATION = -5f
private const val WASHI_OPACITY = 0.93f
private const val WASHI_PATTERN_OPACITY = 0.6f
private const val WASHI_TEETH = 6
private const val WASHI_TOOTH_RATIO = 0.03f

private fun tornStripPath(size: Size): Path = Path().apply {
    val tooth = size.width * WASHI_TOOTH_RATIO
    moveTo(tooth, 0f)
    lineTo(size.width - tooth, 0f)
    for (i in 1..WASHI_TEETH) lineTo(if (i % 2 == 1) size.width else size.width - tooth, size.height * i / WASHI_TEETH)
    lineTo(tooth, size.height)
    for (i in WASHI_TEETH - 1 downTo 0) lineTo(if (i % 2 == 1) 0f else tooth, size.height * i / WASHI_TEETH)
    close()
}

private fun DrawScope.drawWashiPattern(pattern: WashiPattern, color: Color) {
    val unit = size.height / 18f // the tape is 18 units tall in the artwork
    when (pattern) {
        WashiPattern.Dots -> {
            val step = 7 * unit
            var y = step / 2
            while (y < size.height) {
                var x = step / 2
                while (x < size.width) {
                    drawCircle(color, radius = 1.5f * unit, center = Offset(x, y))
                    x += step
                }
                y += step
            }
        }
        WashiPattern.Stripes -> {
            var x = 0f
            while (x < size.width) {
                drawRect(color, topLeft = Offset(x, 0f), size = Size(3 * unit, size.height))
                x += 7 * unit
            }
        }
        WashiPattern.Plain -> Unit
    }
}

/** A pressed daisy: eight petals around a centre. */
@Composable
fun Daisy(
    modifier: Modifier = Modifier,
    size: Dp = LibraryDimens.daisySize,
    petal: Color = LibraryTheme.colors.paper,
    center: Color = LibraryTheme.colors.highlight,
    outline: Color = LibraryTheme.colors.ink,
) {
    Canvas(modifier.size(size)) {
        val c = this.center
        val u = this.size.minDimension / 24f
        val petalSize = Size(5.2f * u, 9.2f * u)
        val petalTopLeft = Offset(c.x - petalSize.width / 2, c.y - 10.6f * u)
        repeat(8) { i ->
            rotate(i * 45f, c) {
                drawOval(petal, petalTopLeft, petalSize)
                drawOval(outline, petalTopLeft, petalSize, style = Stroke(0.7f * u))
            }
        }
        drawCircle(center, radius = 3f * u, center = c)
        drawCircle(outline, radius = 3f * u, center = c, style = Stroke(0.7f * u))
    }
}

/** A pressed sprig: a diagonal stem with pairs of leaves. */
@Composable
fun Sprig(
    modifier: Modifier = Modifier,
    size: Dp = LibraryDimens.sprigSize,
    stem: Color = LibraryTheme.colors.accentDeep,
    leaf: Color = LibraryTheme.colors.accentLight,
) {
    Canvas(modifier.size(size)) {
        val u = this.size.minDimension / 48f
        val stroke = Stroke(1.3f * u)
        drawLine(stem, Offset(8 * u, 42 * u), Offset(40 * u, 6 * u), strokeWidth = 1.3f * u)
        val leafSize = Size(8f * u, 5.2f * u)
        listOf(Offset(14f, 33f), Offset(22f, 24f), Offset(30f, 15f)).forEach { node ->
            val pivot = Offset(node.x * u, node.y * u)
            for (side in listOf(-1, 1)) {
                rotate(-48f + side * 38f, pivot) {
                    val topLeft = Offset(pivot.x, pivot.y - leafSize.height / 2)
                    drawOval(leaf, topLeft, leafSize)
                    drawOval(stem, topLeft, leafSize, style = stroke)
                }
            }
        }
    }
}

/** The wooden shelf the covers stand on. */
@Composable
fun Shelf(
    modifier: Modifier = Modifier,
    color: Color = LibraryTheme.colors.accentDeep,
) {
    val shadow = LibraryTheme.colors.shadow
    Canvas(modifier.fillMaxWidth().height(LibraryDimens.shelfHeight)) {
        val corner = CornerRadius(size.height / 2)
        drawFlatShadow(shadow, corner, LibraryDimens.shadowOffsetSmall)
        drawRoundRect(color, cornerRadius = corner)
    }
}

/**
 * Draws the redesign's flat shadow: the same rounded shape offset downwards,
 * without blur. Much cheaper than elevation in long lists.
 */
internal fun DrawScope.drawFlatShadow(
    color: Color,
    corner: CornerRadius,
    offsetY: Dp = LibraryDimens.shadowOffset,
) {
    drawRoundRect(color, topLeft = Offset(0f, offsetY.toPx()), size = size, cornerRadius = corner)
}

/** The flat shadow for any [shape], e.g. arch-topped cards whose corners differ. */
internal fun DrawScope.drawFlatShadow(
    color: Color,
    shape: Shape,
    offsetY: Dp = LibraryDimens.shadowOffset,
) {
    val outline = shape.createOutline(size, layoutDirection, this)
    translate(top = offsetY.toPx()) { drawOutline(outline, color) }
}
