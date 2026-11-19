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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
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
