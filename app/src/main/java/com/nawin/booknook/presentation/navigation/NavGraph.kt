package com.nawin.booknook.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nawin.booknook.presentation.ui.home.HomeScreen
import com.nawin.booknook.presentation.ui.library.LibraryScreen
import com.nawin.booknook.presentation.ui.search.SearchScreen
import com.nawin.booknook.presentation.ui.calendar.CalendarScreen
import com.nawin.booknook.presentation.ui.journal.JournalScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.Library.route) {
            LibraryScreen(navController = navController)
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }
        composable(Screen.Journal.route) {
            JournalScreen(navController = navController)
        }
        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            // BookDetailScreen(bookId = bookId, navController = navController)
        }
    }
}