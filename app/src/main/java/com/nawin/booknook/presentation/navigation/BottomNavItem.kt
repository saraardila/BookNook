package com.nawin.booknook.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.nawin.booknook.R

/**
 * Tabs of the bottom bar, in display order.
 *
 * Transitional set: Log and Card from the redesign replace Journal and
 * Settings once those screens exist (redesign steps 5 and 7). The calendar
 * is reached from Home.
 */
enum class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
) {
    Home(Screen.Home, Icons.Outlined.Home, R.string.nav_home),
    Shelves(Screen.Library, Icons.AutoMirrored.Outlined.MenuBook, R.string.nav_shelves),
    Search(Screen.Search, Icons.Outlined.Search, R.string.nav_search),
    Journal(Screen.Journal, Icons.Outlined.EditNote, R.string.nav_journal),
    Settings(Screen.Settings, Icons.Outlined.Settings, R.string.nav_settings),
}
