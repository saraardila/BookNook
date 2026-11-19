package com.nawin.booknook.presentation.components.biblioteca

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawin.booknook.presentation.theme.Biblioteca
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Round ink stamp with a double ring and three lines of text, like
 * "LEÍDO / 02 / OCT" in the reading card or "DEVOLVER / 30 / NOV" on the loan card.
 */
@Composable
fun InkStamp(
    top: String,
    big: String,
    bottom: String,
    modifier: Modifier = Modifier,
    size: Dp = 62.dp,
    color: Color = Biblioteca.colors.stamp,
    rotation: Float = -8f,
    background: Color = Color.Transparent,
    struck: Boolean = false,
) {
    val type = Biblioteca.type
    val px = size.value
    Box(
        modifier
            .size(size)
            .rotate(rotation)
            .alpha(0.92f)
            .semantics { contentDescription = "$top $big $bottom" }
            .drawBehind {
                val r = this.size.minDimension / 2
                drawCircle(background)
                drawCircle(color, radius = r - 1.25.dp.toPx(), style = Stroke(2.5.dp.toPx()))
                drawCircle(color, radius = r - 5.5.dp.toPx(), style = Stroke(1.2.dp.toPx()))
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(top, style = type.label.copy(fontSize = (px * 0.13f).sp, letterSpacing = 1.sp), color = color)
            Text(
                big,
                style = type.titleSmall.copy(
                    fontSize = (px * 0.3f).sp,
                    lineHeight = (px * 0.32f).sp,
                    textDecoration = if (struck) TextDecoration.LineThrough else null,
                ),
                color = color,
            )
            Text(bottom, style = type.label.copy(fontSize = (px * 0.12f).sp, letterSpacing = 1.sp), color = color)
        }
    }
}

/** Closed scalloped outline around a circle (rosettes and the wax seal). */
internal fun scallopPath(center: Offset, radius: Float, bumps: Int, depth: Float): Path {
    val path = Path()
    val r = radius * (1 - depth)
    val bumpR = (2 * r * sin(PI / bumps)).toFloat() / 2 * 1.12f
    for (i in 0 until bumps) {
        val a = (2 * PI * i / bumps - PI / 2).toFloat()
        val c = Offset(center.x + r * cos(a), center.y + r * sin(a))
        val startDeg = Math.toDegrees(a.toDouble()).toFloat() - 90f
        path.arcTo(Rect(c, bumpR), startDeg, 180f, forceMoveTo = i == 0)
    }
    path.close()
    return path
}

/**
 * Rosette badge for a shelf licence. [dashed] draws an empty one still to win,
 * [ring] adds the dotted inner ring of a licence already earned.
 */
@Composable
fun Rosette(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    fill: Color = Biblioteca.colors.butter,
    rotation: Float = 0f,
    dashed: Boolean = false,
    ring: Color? = null,
    content: @Composable () -> Unit = {},
) {
    val outline = Biblioteca.colors.ink
    Box(
        modifier
            .size(size)
            .rotate(rotation)
            .drawBehind {
                val path = scallopPath(center, this.size.minDimension / 2, bumps = 16, depth = 0.09f)
                if (!dashed) drawPath(path, fill)
                drawPath(
                    path, outline,
                    style = Stroke(
                        width = 1.4.dp.toPx(),
                        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.5.dp.toPx())) else null,
                    ),
                )
                if (ring != null) {
                    drawCircle(
                        ring, radius = this.size.minDimension * 0.31f,
                        style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx()))),
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Wax seal: a wobbly deep-coloured disc with a light inner ring. */
@Composable
fun WaxSeal(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    color: Color = Biblioteca.colors.stamp,
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier
            .size(size)
            .drawBehind {
                drawPath(scallopPath(center, this.size.minDimension / 2, bumps = 22, depth = 0.05f), color)
                drawCircle(Color.White.copy(alpha = 0.35f), radius = this.size.minDimension * 0.33f, style = Stroke(1.5.dp.toPx()))
            },
        contentAlignment = Alignment.Center,
    ) { content() }
}
