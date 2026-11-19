package com.nawin.booknook.presentation.components.biblioteca

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.nawin.booknook.presentation.theme.Biblioteca
import com.nawin.booknook.presentation.theme.BibliotecaShapes

/**
 * The basic card: paper, a thin ink outline and a flat shadow 4dp below.
 * The shadow is drawn, not computed (no elevation blur), which keeps long
 * lists smooth.
 */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    color: Color = Biblioteca.colors.paper,
    radius: Int = 18,
    content: @Composable BoxScope.() -> Unit,
) {
    val c = Biblioteca.colors
    val shape = RoundedCornerShape(radius.dp)
    Box(
        modifier
            .drawBehind { drawFlatShadow(c.shadow, CornerRadius(radius.dp.toPx())) }
            .background(color, shape)
            .border(1.5.dp, c.line, shape),
        content = content,
    )
}

/**
 * Cottage-window arch with a double ring, used behind covers on the welcome,
 * shelf and book screens.
 */
@Composable
fun ArchFrame(
    modifier: Modifier = Modifier,
    fill: Color = Biblioteca.colors.primaryLight,
    ring: Color = Biblioteca.colors.primary,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val shape: Shape = BibliotecaShapes.arch
    Box(modifier.border(1.5.dp, ring, shape).padding(6.dp)) {
        Box(Modifier.matchParentSize().clip(shape).background(fill), content = content)
    }
}

/** Shelf label holder: the brass frame with two screws that names a shelf. */
@Composable
fun ShelfLabel(text: String, modifier: Modifier = Modifier) {
    val c = Biblioteca.colors
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier
            .drawBehind { drawFlatShadow(c.shadow, CornerRadius(12.dp.toPx()), offsetY = 3.dp) }
            .background(c.paper, shape)
            .border(1.5.dp, c.line, shape)
            .padding(3.dp)
            .border(1.dp, c.primaryMid, RoundedCornerShape(9.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Screw()
        Text(text, style = Biblioteca.type.titleSmall.copy(lineHeight = Biblioteca.type.titleSmall.fontSize), color = c.ink)
        Screw()
    }
}

@Composable
private fun Screw() = Box(Modifier.size(6.dp).background(Biblioteca.colors.accentDeep, RoundedCornerShape(50)))

/** Spine label (tejuelo) with the shelf code, laid on the bottom corner of a cover. */
@Composable
fun SpineLabel(code: String, author: String, modifier: Modifier = Modifier) {
    val c = Biblioteca.colors
    Box(
        modifier
            .background(Color(0xFFFFFDF6), RoundedCornerShape(4.dp))
            .drawBehind { drawRect(c.primary, size = size.copy(height = 2.5.dp.toPx())) }
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text(
            "$code\n$author",
            style = Biblioteca.type.label.copy(fontSize = Biblioteca.type.label.fontSize * 0.7f, letterSpacing = Biblioteca.type.label.letterSpacing * 0.2f),
            color = Color(0xFF3B2A2E),
        )
    }
}

/** Reading progress: a rounded track with the read part filled. */
@Composable
fun ReadingProgress(fraction: Float, modifier: Modifier = Modifier) {
    val c = Biblioteca.colors
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .fillMaxWidth()
            .height(9.dp)
            .clip(shape)
            .background(c.accentLight)
            .border(1.3.dp, c.line, shape),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(9.dp)
                .background(c.accentDeep),
        )
    }
}
