package com.nawin.booknook.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Opens a bottom-bar destination the way a tab should behave: one copy of
 * each tab, the start destination at the root, and each tab's back stack
 * saved and restored when switching.
 *
 * Use it for every route that is also a tab, including buttons inside a
 * screen (e.g. "Search a book" on Home). Navigating to a tab with a plain
 * `navigate` pushes it onto the current tab's stack, and that stack is later
 * restored in the wrong place.
 */
fun NavController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
