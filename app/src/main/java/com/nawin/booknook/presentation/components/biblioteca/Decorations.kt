package com.nawin.booknook.presentation.components.biblioteca

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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nawin.booknook.presentation.theme.Biblioteca

// Small hand-drawn touches of the redesign. They are all plain Canvas drawing
// (no images, no blur), so they cost almost nothing to render in a list.

/**
 * Lace edge: a coloured band whose bottom border is a row of scallops with
 * little holes, like a paper doily. Put it behind a screen header.
 */
@Composable
fun LaceHeader(
    height: Dp,
    modifier: Modifier = Modifier,
    color: Color = Biblioteca.colors.primaryLight,
    holeColor: Color = Biblioteca.colors.background,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                drawRect(color)
                val step = 20.dp.toPx()
                val r = 9.dp.toPx()
                var x = step / 2
                while (x < size.width + step) {
                    // the scallops hang below the band: drawing outside the bounds is fine
                    drawCircle(color, radius = r, center = Offset(x, size.height))
                    drawCircle(holeColor, radius = 1.8.dp.toPx(), center = Offset(x, size.height - 3.dp.toPx()))
                    x += step
                }
            },
    )
}

enum class WashiPattern { Dots, Stripes, Plain }

/** A strip of washi tape with torn ends. Place it with an offset over a card's edge. */
@Composable
fun WashiTape(
    modifier: Modifier = Modifier,
    width: Dp = 72.dp,
    rotation: Float = -5f,
    color: Color = Biblioteca.colors.washi,
    pattern: WashiPattern = WashiPattern.Dots,
) {
    Canvas(
        modifier
            .width(width)
            .height(18.dp)
            .rotate(rotation),
    ) {
        val w = size.width
        val h = size.height
        val tooth = w * 0.03f
        // zig-zag on both short sides, straight on the long ones
        val path = Path().apply {
            moveTo(tooth, 0f)
            lineTo(w - tooth, 0f)
            for (i in 1..6) lineTo(if (i % 2 == 1) w else w - tooth, h * i / 6f)
            lineTo(tooth, h)
            for (i in 5 downTo 0) lineTo(if (i % 2 == 1) 0f else tooth, h * i / 6f)
            close()
        }
        clipPath(path) {
            drawRect(color.copy(alpha = 0.93f))
            val light = Color.White.copy(alpha = 0.6f)
            when (pattern) {
                WashiPattern.Dots -> {
                    val s = 7.dp.toPx()
                    var y = s / 2
                    while (y < h) {
                        var x = s / 2
                        while (x < w) {
                            drawCircle(light, radius = 1.5.dp.toPx(), center = Offset(x, y)); x += s
                        }
                        y += s
                    }
                }
                WashiPattern.Stripes -> {
                    var x = 0f
                    while (x < w) {
                        drawRect(light, topLeft = Offset(x, 0f), size = Size(3.dp.toPx(), h)); x += 7.dp.toPx()
                    }
                }
                WashiPattern.Plain -> Unit
            }
        }
    }
}

/** A pressed daisy: eight petals around a yellow centre. */
@Composable
fun Daisy(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    petal: Color = Biblioteca.colors.paper,
    center: Color = Biblioteca.colors.butter,
    outline: Color = Biblioteca.colors.ink,
) {
    Canvas(modifier.size(size)) {
        val c = this.center
        val u = this.size.minDimension / 24f
        repeat(8) { i ->
            rotate(i * 45f, c) {
                val topLeft = Offset(c.x - 2.6f * u, c.y - 10.6f * u)
                val petalSize = Size(5.2f * u, 9.2f * u)
                drawOval(petal, topLeft, petalSize)
                drawOval(outline, topLeft, petalSize, style = Stroke(0.7f * u))
            }
        }
        drawCircle(center, radius = 3f * u, center = c)
        drawCircle(outline, radius = 3f * u, center = c, style = Stroke(0.7f * u))
    }
}

/** A pressed sprig: a curved stem with leaves on both sides. */
@Composable
fun Sprig(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    stem: Color = Biblioteca.colors.accentDeep,
    leaf: Color = Biblioteca.colors.accentLight,
) {
    Canvas(modifier.size(size)) {
        val u = this.size.minDimension / 48f
        val stroke = Stroke(1.3f * u)
        drawLine(stem, Offset(8 * u, 42 * u), Offset(40 * u, 6 * u), strokeWidth = 1.3f * u)
        // leaves: pairs along the stem, each an oval tilted away from it
        listOf(Offset(14f, 33f), Offset(22f, 24f), Offset(30f, 15f)).forEach { p ->
            for (side in listOf(-1, 1)) {
                rotate(-48f + side * 38f, Offset(p.x * u, p.y * u)) {
                    val tl = Offset(p.x * u, p.y * u - 2.6f * u)
                    val sz = Size(8f * u, 5.2f * u)
                    drawOval(leaf, tl, sz)
                    drawOval(stem, tl, sz, style = stroke)
                }
            }
        }
    }
}

/** The wooden shelf the covers stand on. */
@Composable
fun Shelf(modifier: Modifier = Modifier, color: Color = Biblioteca.colors.accentDeep) {
    val shadow = Biblioteca.colors.shadow
    Canvas(modifier.fillMaxWidth().height(9.dp)) {
        drawFlatShadow(shadow, CornerRadius(size.height / 2))
        drawRoundRect(color, cornerRadius = CornerRadius(size.height / 2))
    }
}

/** The flat, unblurred 4dp shadow under cards and buttons. */
internal fun DrawScope.drawFlatShadow(color: Color, corner: CornerRadius, offsetY: Dp = 4.dp) {
    drawRoundRect(color, topLeft = Offset(0f, offsetY.toPx()), size = size, cornerRadius = corner)
}
