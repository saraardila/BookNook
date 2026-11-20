package com.nawin.booknook.presentation.components.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.semantics.Role
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.theme.ShelfPalette

/**
 * A shelf theme as a miniature screen (header band, a white card and two
 * colour dots) with its [name] below. The selected one wears a wax seal.
 * Announced as a radio button: place several inside a selectableGroup.
 */
@Composable
fun ThemeMini(
    palette: ShelfPalette,
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val shape = LibraryShapes.label
    Column(
        modifier
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(THEME_MINI_ASPECT)
                .drawBehind { drawFlatShadow(colors.shadow, CornerRadius(LibraryDimens.radiusLabel.toPx()), LibraryDimens.shadowOffsetSmall) }
                .clip(shape)
                .background(palette.background)
                .border(
                    if (selected) LibraryDimens.strokeStamp else LibraryDimens.stroke,
                    if (selected) colors.ink else colors.outline,
                    shape,
                ),
        ) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().weight(THEME_MINI_HEADER).background(palette.primary))
                Spacer(Modifier.weight(THEME_MINI_GAP))
                Box(
                    Modifier
                        .fillMaxWidth(THEME_MINI_CARD_WIDTH)
                        .weight(THEME_MINI_CARD)
                        .background(colors.photoFrame, LibraryShapes.cover)
                        .border(LibraryDimens.strokeHairline, colors.outline, LibraryShapes.cover),
                )
                Spacer(Modifier.weight(THEME_MINI_GAP))
                Row(horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXxs)) {
                    Box(
                        Modifier
                            .size(LibraryDimens.themeMiniDot)
                            .background(palette.tint, LibraryShapes.pill)
                            .border(LibraryDimens.strokeHairline, colors.outline, LibraryShapes.pill),
                    )
                    Box(Modifier.size(LibraryDimens.themeMiniDot).background(palette.primary, LibraryShapes.pill))
                }
                Spacer(Modifier.weight(THEME_MINI_FOOT))
            }
            if (selected) {
                WaxSeal(
                    Modifier.align(Alignment.TopEnd).padding(LibraryDimens.spaceXxs),
                    size = LibraryDimens.themeMiniSeal,
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(LibraryDimens.spaceMd))
                }
            }
        }
        Text(
            name,
            style = LibraryTheme.typography.navLabel,
            color = if (selected) colors.ink else colors.inkMuted,
            maxLines = 1,
        )
    }
}

// Proportions of the miniature screen, from the design (height = 1.45 × width).
private const val THEME_MINI_ASPECT = 1f / 1.45f
private const val THEME_MINI_HEADER = 0.30f
private const val THEME_MINI_GAP = 0.08f
private const val THEME_MINI_CARD = 0.22f
private const val THEME_MINI_CARD_WIDTH = 0.72f
private const val THEME_MINI_FOOT = 0.22f
