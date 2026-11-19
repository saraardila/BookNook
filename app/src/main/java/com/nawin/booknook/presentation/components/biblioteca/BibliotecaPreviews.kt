package com.nawin.booknook.presentation.components.biblioteca

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nawin.booknook.presentation.theme.Biblioteca
import com.nawin.booknook.presentation.theme.BibliotecaTheme

// Previews only: open this file in Android Studio's Design view to see every
// piece of the redesign together, in the vanilla palette.

@Preview(name = "Componentes · vainilla", widthDp = 390, heightDp = 900, showBackground = true)
@Composable
private fun ComponentsPreview() = BibliotecaTheme {
    val c = Biblioteca.colors
    val t = Biblioteca.type
    Column(Modifier.background(c.background).fillMaxWidth()) {
        Box {
            LaceHeader(height = 120.dp)
            Column(Modifier.padding(start = 22.dp, top = 36.dp)) {
                Text("buenas tardes, Sara", style = t.hand, color = c.stamp)
                Text("Mi biblioteca", style = t.title, color = c.ink)
            }
        }
        Spacer(Modifier.height(28.dp))
        Column(Modifier.padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Box {
                PaperCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("FICHA DE PRÉSTAMO · Nº 14", style = t.label, color = c.primary)
                        Text("Throne of Glass", style = t.titleSmall.copy(fontSize = t.titleSmall.fontSize * 1.2f), color = c.ink, modifier = Modifier.padding(top = 8.dp))
                        Text("cap. 31 · ¡no puedo parar!", style = t.hand.copy(fontSize = t.hand.fontSize * 0.8f), color = c.washi)
                        Spacer(Modifier.height(8.dp))
                        ReadingProgress(0.52f, Modifier.width(200.dp))
                    }
                }
                WashiTape(Modifier.offset(x = 24.dp, y = (-9).dp))
                InkStamp("DEVOLVER", "30", "NOV", Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-18).dp), size = 64.dp, rotation = 12f, background = c.paper)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Rosette(fill = c.butter, rotation = -6f) { Text("3/5", style = t.label, color = c.ink) }
                Rosette(fill = c.stamp, ring = c.onPrimary, rotation = 4f) { Text("✓", style = t.titleSmall, color = c.onPrimary) }
                Rosette(dashed = true) { Text("?", style = t.hand, color = c.muted) }
                InkStamp("LEÍDO", "02", "OCT")
                WaxSeal(size = 56.dp) { Text("♥", color = c.onPrimary) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ShelfLabel("Romantasy")
                Daisy()
                Daisy(petal = c.primaryMid)
                Sprig()
            }
            ArchFrame(Modifier.size(width = 200.dp, height = 170.dp)) {
                Column(Modifier.align(Alignment.BottomCenter).padding(horizontal = 18.dp, vertical = 18.dp)) {
                    Box(Modifier.align(Alignment.End)) { SpineLabel("ROM", "MAA") }
                    Spacer(Modifier.height(6.dp))
                    Shelf()
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LibraryChip("Romantasy", selected = true, onClick = {})
                LibraryChip("Fantasía", onClick = {})
                LibraryChip("slow burn")
            }
            PillButton("Hacerme el carnet", onClick = {}, modifier = Modifier.fillMaxWidth())
            PillButton("Ya tengo carnet", onClick = {}, primary = false, modifier = Modifier.fillMaxWidth())
            PillNavBar(
                tabs = listOf(
                    NavTab("Inicio", Icons.Outlined.Home),
                    NavTab("Estantes", Icons.AutoMirrored.Outlined.MenuBook),
                    NavTab("Anotar", Icons.Outlined.Add, highlighted = true),
                    NavTab("Buscar", Icons.Outlined.Search),
                    NavTab("Carnet", Icons.Outlined.Badge),
                ),
                selected = 0,
                onSelect = {},
            )
        }
    }
}
