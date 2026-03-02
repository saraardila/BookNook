package com.nawin.booknook.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
) {
    object Home    : BottomNavItem(Screen.Home,     Icons.Default.Home,        "Home")
    object Search  : BottomNavItem(Screen.Search,   Icons.Default.Search,      "Search")
    object Library : BottomNavItem(Screen.Library,  Icons.Default.Book,        "Library")
    object Calendar: BottomNavItem(Screen.Calendar, Icons.Default.CalendarMonth,"Calendar")
    object Journal : BottomNavItem(Screen.Journal,  Icons.Default.EditNote,    "Journal")
}