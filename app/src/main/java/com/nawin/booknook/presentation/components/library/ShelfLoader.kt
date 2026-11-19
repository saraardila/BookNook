package com.nawin.booknook.presentation.components.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.nawin.booknook.R
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

/**
 * Loading indicator in the library style: three book spines on a shelf that
 * lift in turn, as if someone were looking for one, over a handwritten caption.
 *
 * It only appears once loading has taken longer than [showAfterMillis], so fast
 * loads (a local database read) do not flash it. The animated value is read in
 * the draw phase only, so each frame redraws without recomposing.
 */
@Composable
fun ShelfLoader(
    modifier: Modifier = Modifier,
    caption: String? = stringResource(R.string.loading_shelves),
    showAfterMillis: Long = LOADER_DELAY_MILLIS,
) {
    var visible by remember { mutableStateOf(showAfterMillis <= 0) }
    LaunchedEffect(showAfterMillis) {
        delay(showAfterMillis)
        visible = true
    }
    val description = stringResource(R.string.cd_loading)

    AnimatedVisibility(visible, modifier, enter = fadeIn(), exit = fadeOut()) {
        Column(
            Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = description },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
        ) {
            BouncingSpines()
            if (caption != null) {
                Text(caption, style = LibraryTheme.typography.handSmall, color = LibraryTheme.colors.inkMuted)
            }
        }
    }
}

@Composable
private fun BouncingSpines() {
    val colors = LibraryTheme.colors
    val spineColors = listOf(colors.primary, colors.accent, colors.blush)
    val progress by rememberInfiniteTransition(label = "shelf_loader").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(LOADER_CYCLE_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "spines",
    )

    Canvas(Modifier.size(LibraryDimens.loaderWidth, LibraryDimens.loaderHeight)) {
        val stroke = LibraryDimens.strokeThin.toPx()
        val shelfHeight = LibraryDimens.shelfHeight.toPx() / 2
        val spineWidth = LibraryDimens.loaderSpineWidth.toPx()
        val gap = LibraryDimens.spaceXs.toPx()
        val lift = LibraryDimens.loaderLift.toPx()
        val shelfTop = size.height - shelfHeight
        val rowWidth = SPINE_HEIGHTS.size * spineWidth + (SPINE_HEIGHTS.size - 1) * gap
        var left = (size.width - rowWidth) / 2

        SPINE_HEIGHTS.forEachIndexed { index, heightRatio ->
            // Each spine rises on its own third of the cycle and rests the rest of it.
            val phase = (progress - index.toFloat() / SPINE_HEIGHTS.size) * 2 * PI
            val rise = max(0f, sin(phase).toFloat()) * lift
            val height = (shelfTop - lift) * heightRatio
            val topLeft = Offset(left, shelfTop - height - rise)
            val spineSize = Size(spineWidth, height)
            val corner = CornerRadius(stroke * 2)
            drawRoundRect(spineColors[index], topLeft, spineSize, corner)
            drawRoundRect(colors.ink, topLeft, spineSize, corner, style = Stroke(stroke))
            // Two bands across the spine, like the gilt lines on a hardback.
            listOf(BAND_TOP, BAND_BOTTOM).forEach { band ->
                val y = topLeft.y + height * band
                drawLine(colors.ink, Offset(left, y), Offset(left + spineWidth, y), stroke)
            }
            left += spineWidth + gap
        }
        drawRoundRect(colors.accentDeep, Offset(0f, shelfTop), Size(size.width, shelfHeight), CornerRadius(shelfHeight / 2))
    }
}

/** How long a load may take before the loader shows. */
const val LOADER_DELAY_MILLIS = 400L
private const val LOADER_CYCLE_MILLIS = 1_200
private val SPINE_HEIGHTS = listOf(0.9f, 0.72f, 1f)
private const val BAND_TOP = 0.18f
private const val BAND_BOTTOM = 0.82f

// Shown at once (no delay) so the animation can be inspected in the preview pane.
@Preview(name = "Shelf loader", locale = "es", showBackground = true)
@Composable
private fun ShelfLoaderPreview() = LibraryTheme {
    ShelfLoader(showAfterMillis = 0)
}
