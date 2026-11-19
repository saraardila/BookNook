package com.nawin.booknook.presentation.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nawin.booknook.presentation.components.library.NavTab
import com.nawin.booknook.presentation.components.library.PillNavBar
import com.nawin.booknook.presentation.theme.LibraryDimens

/**
 * Bottom bar wired to the navigation graph. Hidden on screens that are not
 * a tab (book detail, calendar), so they get the full height.
 */
@Composable
fun LibraryBottomBar(navController: NavHostController, modifier: Modifier = Modifier) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val items = BottomNavItem.entries
    val selectedIndex = items.indexOfFirst { it.screen.route == currentRoute }
    if (selectedIndex < 0) return

    PillNavBar(
        tabs = items.map { NavTab(stringResource(it.labelRes), it.icon) },
        selectedIndex = selectedIndex,
        onTabSelected = { index -> navController.navigateToTopLevel(items[index].screen.route) },
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = LibraryDimens.navBarMargin, vertical = LibraryDimens.spaceMd),
    )
}
