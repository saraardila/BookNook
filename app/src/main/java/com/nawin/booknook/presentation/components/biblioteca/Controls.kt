package com.nawin.booknook.presentation.components.biblioteca

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawin.booknook.presentation.theme.Biblioteca
import com.nawin.booknook.presentation.theme.BibliotecaShapes

/** Rounded pill button. [primary] is the filled one; the other is paper with an outline. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true,
) {
    val c = Biblioteca.colors
    val shape = BibliotecaShapes.pill
    Box(
        modifier
            .heightIn(min = 52.dp)
            .drawBehind { drawFlatShadow(c.shadow, CornerRadius(size.height / 2)) }
            .clip(shape)
            .background(if (primary) c.primary else c.paper)
            .border(1.5.dp, c.line, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = Biblioteca.type.button, color = if (primary) c.onPrimary else c.ink)
    }
}

/** Filter or tag chip. Selected chips are filled with the primary colour. */
@Composable
fun LibraryChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val c = Biblioteca.colors
    val shape = BibliotecaShapes.pill
    val base = modifier
        .clip(shape)
        .background(if (selected) c.primary else c.paper)
        .border(1.5.dp, c.line, shape)
    val clickable = if (onClick != null) base.selectable(selected = selected, role = Role.Checkbox, onClick = onClick) else base
    Box(clickable.heightIn(min = 36.dp).padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, style = Biblioteca.type.button.copy(fontSize = Biblioteca.type.button.fontSize * 0.8f), color = if (selected) c.onPrimary else c.ink)
    }
}

/** One tab of [PillNavBar]. */
data class NavTab(val label: String, val icon: ImageVector, val highlighted: Boolean = false)

/**
 * Bottom bar: a floating paper pill with five labelled tabs. The selected tab
 * gets a filled circle; a [NavTab.highlighted] one (Anotar) always shows a
 * butter circle so the main action stands out.
 */
@Composable
fun PillNavBar(
    tabs: List<NavTab>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PaperCard(modifier.fillMaxWidth().height(66.dp), radius = 33) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp).selectableGroup(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { i, tab -> NavItem(tab, i == selected) { onSelect(i) } }
        }
    }
}

@Composable
private fun RowScope.NavItem(tab: NavTab, selected: Boolean, onClick: () -> Unit) {
    val c = Biblioteca.colors
    Column(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val circle = when {
            selected -> Modifier.background(c.primary, RoundedCornerShape(50))
            tab.highlighted -> Modifier.background(c.butter, RoundedCornerShape(50)).border(1.5.dp, c.line, RoundedCornerShape(50))
            else -> Modifier
        }
        Box(Modifier.size(34.dp).then(circle), contentAlignment = Alignment.Center) {
            Icon(tab.icon, contentDescription = null, tint = if (selected) c.onPrimary else c.ink, modifier = Modifier.size(20.dp))
        }
        Text(
            tab.label,
            style = Biblioteca.type.label.copy(fontSize = Biblioteca.type.label.fontSize * 0.95f, letterSpacing = 0.sp),
            color = if (selected) c.primary else c.muted,
        )
    }
}
