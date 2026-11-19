package com.nawin.booknook.presentation.components.library

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme

/**
 * The basic card: paper, a thin ink outline and a flat shadow below it.
 * [cornerRadius] must match the shape so the drawn shadow follows the card.
 */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    color: Color = LibraryTheme.colors.paper,
    cornerRadius: Dp = LibraryDimens.radiusCard,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = LibraryTheme.colors
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier
            .drawBehind { drawFlatShadow(colors.shadow, CornerRadius(cornerRadius.toPx())) }
            .background(color, shape)
            .border(LibraryDimens.stroke, colors.outline, shape),
        content = content,
    )
}

/** Cottage-window arch with a double ring, shown behind covers. */
@Composable
fun ArchFrame(
    modifier: Modifier = Modifier,
    fill: Color = LibraryTheme.colors.primaryLight,
    ring: Color = LibraryTheme.colors.primary,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val shape: Shape = LibraryShapes.arch
    Box(modifier.border(LibraryDimens.stroke, ring, shape).padding(LibraryDimens.archRingGap)) {
        Box(Modifier.matchParentSize().clip(shape).background(fill), content = content)
    }
}

/** Shelf label holder: a small frame with two screws that names a shelf. */
@Composable
fun ShelfLabel(text: String, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val titleSmall = LibraryTheme.typography.titleSmall
    Row(
        modifier
            .drawBehind { drawFlatShadow(colors.shadow, CornerRadius(LibraryDimens.radiusLabel.toPx()), LibraryDimens.shadowOffsetSmall) }
            .background(colors.paper, LibraryShapes.label)
            .border(LibraryDimens.stroke, colors.outline, LibraryShapes.label)
            .padding(LibraryDimens.spaceXs - LibraryDimens.strokeHairline)
            .border(LibraryDimens.strokeHairline, colors.primaryMid, LibraryShapes.labelInner)
            .padding(horizontal = LibraryDimens.spaceSm + LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
    ) {
        Screw()
        Text(text, style = titleSmall.copy(lineHeight = titleSmall.fontSize), color = colors.ink)
        Screw()
    }
}

@Composable
private fun Screw() = Box(
    Modifier
        .size(LibraryDimens.screwSize)
        .background(LibraryTheme.colors.accentDeep, LibraryShapes.pill),
)

/**
 * Spine label (the library call-number sticker) for the bottom corner of a
 * cover: a shelf code over the first letters of the author.
 */
@Composable
fun SpineLabel(shelfCode: String, authorCode: String, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    Box(
        modifier
            .background(colors.spineLabel, LibraryShapes.cover)
            .drawBehind { drawRect(colors.primary, size = size.copy(height = LibraryDimens.strokeStamp.toPx())) }
            .padding(horizontal = LibraryDimens.spaceXs, vertical = LibraryDimens.spaceXxs),
    ) {
        Text(
            text = "$shelfCode\n$authorCode",
            style = LibraryTheme.typography.label.copy(fontSize = SPINE_LABEL_TEXT, letterSpacing = SPINE_LABEL_TRACKING, lineHeight = SPINE_LABEL_TEXT),
            color = colors.spineLabelInk,
        )
    }
}

private val SPINE_LABEL_TEXT = 7.sp
private val SPINE_LABEL_TRACKING = 0.3.sp

/** Reading progress: a rounded track with the read part filled. */
@Composable
fun ReadingProgress(fraction: Float, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(LibraryDimens.progressHeight)
            .clip(LibraryShapes.pill)
            .background(colors.accentLight)
            .border(LibraryDimens.strokeThin, colors.outline, LibraryShapes.pill),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(LibraryDimens.progressHeight)
                .background(colors.accentDeep),
        )
    }
}
