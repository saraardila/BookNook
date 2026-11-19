package com.nawin.booknook.presentation.components.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.roundToInt
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme

/** Pill-shaped button. The [primary] variant is filled; the other is paper with an outline. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true,
) {
    val colors = LibraryTheme.colors
    Box(
        modifier
            .heightIn(min = LibraryDimens.buttonMinHeight)
            .drawBehind { drawFlatShadow(colors.shadow, CornerRadius(size.height / 2)) }
            .clip(LibraryShapes.pill)
            .background(if (primary) colors.primary else colors.paper)
            .border(LibraryDimens.stroke, colors.outline, LibraryShapes.pill)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = LibraryDimens.spaceXl, vertical = LibraryDimens.spaceMd),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = LibraryTheme.typography.button, color = if (primary) colors.onPrimary else colors.ink)
    }
}

/**
 * Tag or filter chip. With [onClick] it becomes a toggle (announced as a
 * checkbox); without it, it is a read-only tag.
 */
@Composable
fun LibraryChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = LibraryTheme.colors
    val base = modifier
        .heightIn(min = LibraryDimens.chipMinHeight)
        .clip(LibraryShapes.pill)
        .background(if (selected) colors.primary else colors.paper)
        .border(LibraryDimens.stroke, colors.outline, LibraryShapes.pill)
    val interactive = if (onClick != null) base.selectable(selected = selected, role = Role.Checkbox, onClick = onClick) else base
    Box(
        interactive.padding(horizontal = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceSm),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = LibraryTheme.typography.chip, color = if (selected) colors.onPrimary else colors.ink)
    }
}

/**
 * Round paper button with an icon and the flat shadow (add, back, favourite…).
 * The touch area grows to the minimum interactive size around the drawn circle.
 */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Color = LibraryTheme.colors.paper,
    tint: Color = LibraryTheme.colors.ink,
) {
    val colors = LibraryTheme.colors
    Box(
        modifier
            .minimumInteractiveComponentSize()
            .size(LibraryDimens.roundButton)
            .drawBehind { drawFlatShadow(colors.shadow, CornerRadius(size.height / 2), LibraryDimens.shadowOffsetSmall) }
            .clip(LibraryShapes.pill)
            .background(fill)
            .border(LibraryDimens.stroke, colors.outline, LibraryShapes.pill)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(LibraryDimens.navIcon))
    }
}

/**
 * A row of mutually exclusive options inside one paper pill; the selected one is
 * filled. [selectedIndex] may be null when none of the options applies. Each
 * option is announced as a radio button of the same group.
 */
@Composable
fun SegmentedPills(
    options: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .drawBehind { drawFlatShadow(colors.shadow, CornerRadius(size.height / 2)) }
            .background(colors.paper, LibraryShapes.pill)
            .border(LibraryDimens.stroke, colors.outline, LibraryShapes.pill)
            .padding(LibraryDimens.spaceXs)
            .selectableGroup(),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = LibraryDimens.chipMinHeight)
                    .clip(LibraryShapes.pill)
                    .background(if (selected) colors.primary else Color.Transparent)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(index) })
                    .padding(horizontal = LibraryDimens.spaceXs),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    option,
                    style = LibraryTheme.typography.chip,
                    color = if (selected) colors.onPrimary else colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Small read-only pill naming a book's reading status, e.g. READING. */
@Composable
fun StatusTag(text: String, color: Color, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    Text(
        text = text,
        style = LibraryTheme.typography.label,
        color = colors.ink,
        maxLines = 1,
        modifier = modifier
            .background(color, LibraryShapes.pill)
            .border(LibraryDimens.strokeHairline, colors.outline, LibraryShapes.pill)
            .padding(horizontal = LibraryDimens.spaceSm, vertical = LibraryDimens.spaceXxs),
    )
}

/**
 * Read-only rating out of [RATING_MAX] stars, rounded to whole stars.
 * [contentDescription] is read instead of the individual stars.
 */
@Composable
fun StarRating(rating: Float, contentDescription: String, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val filled = rating.roundToInt().coerceIn(0, RATING_MAX)
    Row(modifier.semantics(mergeDescendants = true) { this.contentDescription = contentDescription }) {
        repeat(RATING_MAX) { index ->
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < filled) colors.primary else colors.primary.copy(alpha = EMPTY_STAR_ALPHA),
                modifier = Modifier.size(LibraryDimens.starSize),
            )
        }
    }
}

/**
 * Editable rating: one star button per point, each at least the minimum touch
 * size. Tapping the current rating again clears it. Each star is announced as a
 * radio button with [starDescription] (e.g. "3 of 5 stars").
 */
@Composable
fun RatingInput(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    starDescription: @Composable (stars: Int) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val current = rating.roundToInt().coerceIn(0, RATING_MAX)
    Row(modifier.selectableGroup()) {
        for (stars in 1..RATING_MAX) {
            val description = starDescription(stars)
            Box(
                Modifier
                    .minimumInteractiveComponentSize()
                    .clip(LibraryShapes.pill)
                    .selectable(
                        selected = stars == current,
                        role = Role.RadioButton,
                        onClick = { onRatingChange(if (stars == current) 0f else stars.toFloat()) },
                    )
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (stars <= current) colors.primary else colors.primary.copy(alpha = EMPTY_STAR_ALPHA),
                    modifier = Modifier.size(LibraryDimens.ratingStar),
                )
            }
        }
    }
}

/** Highest rating a book can get. */
const val RATING_MAX = 5
private const val EMPTY_STAR_ALPHA = 0.28f

/** One tab of [PillNavBar]. [highlighted] keeps a filled circle even when not selected. */
data class NavTab(val label: String, val icon: ImageVector, val highlighted: Boolean = false)

/**
 * Bottom navigation: a floating paper pill with labelled tabs. The selected
 * tab gets a filled circle in the primary colour.
 */
@Composable
fun PillNavBar(
    tabs: List<NavTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PaperCard(
        modifier.fillMaxWidth().height(LibraryDimens.navBarHeight),
        cornerRadius = LibraryDimens.navBarHeight / 2,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = LibraryDimens.spaceSm)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                NavItem(tab, selected = index == selectedIndex) { onTabSelected(index) }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(tab: NavTab, selected: Boolean, onClick: () -> Unit) {
    val colors = LibraryTheme.colors
    val circle = when {
        selected -> Modifier.background(colors.primary, LibraryShapes.pill)
        tab.highlighted -> Modifier
            .background(colors.highlight, LibraryShapes.pill)
            .border(LibraryDimens.stroke, colors.outline, LibraryShapes.pill)
        else -> Modifier
    }
    Column(
        Modifier
            .weight(1f)
            .heightIn(min = LibraryDimens.touchTarget)
            .clip(LibraryShapes.row)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = LibraryDimens.spaceXs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXxs),
    ) {
        Box(Modifier.size(LibraryDimens.navIconContainer).then(circle), contentAlignment = Alignment.Center) {
            // The label below already names the tab, so the icon is decorative.
            Icon(
                tab.icon,
                contentDescription = null,
                tint = if (selected) colors.onPrimary else colors.ink,
                modifier = Modifier.size(LibraryDimens.navIcon),
            )
        }
        Text(tab.label, style = LibraryTheme.typography.navLabel, color = if (selected) colors.primary else colors.inkMuted)
    }
}
