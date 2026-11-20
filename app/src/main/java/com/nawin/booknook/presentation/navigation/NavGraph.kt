package com.nawin.booknook.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nawin.booknook.presentation.ui.bookdetail.BookDetailScreen
import com.nawin.booknook.presentation.ui.calendar.CalendarScreen
import com.nawin.booknook.presentation.ui.home.HomeScreen
import com.nawin.booknook.presentation.ui.journal.JournalScreen
import com.nawin.booknook.presentation.ui.library.LibraryScreen
import com.nawin.booknook.presentation.ui.search.SearchScreen
import com.nawin.booknook.presentation.ui.newstamp.NewStampScreen
import com.nawin.booknook.presentation.ui.newstamp.NewStampViewModel
import com.nawin.booknook.presentation.ui.progress.ProgressScreen
import com.nawin.booknook.presentation.ui.readingcard.ReadingCardScreen
import com.nawin.booknook.presentation.ui.settings.SettingsScreen
import com.nawin.booknook.presentation.ui.share.ShareScreen
import com.nawin.booknook.presentation.ui.share.ShareViewModel

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
    ) {
        // Redesigned screens handle the status bar themselves.
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Library.route) {
            LibraryScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.Journal.route) {
            JournalScreen(navController = navController)
        }

        // Screens not redesigned yet keep the top inset they always had.
        composable(Screen.Calendar.route) {
            LegacyScreen { CalendarScreen(navController = navController) }
        }
        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId").orEmpty()
            BookDetailScreen(bookId = bookId, navController = navController)
        }
        composable(Screen.ReadingCard.route) {
            ReadingCardScreen(navController = navController)
        }
        composable(
            route = Screen.NewStamp.route,
            arguments = listOf(navArgument(NewStampViewModel.BOOK_ID_ARG) { type = NavType.StringType }),
        ) {
            NewStampScreen(navController = navController)
        }
        composable(
            route = Screen.Share.route,
            arguments = listOf(navArgument(ShareViewModel.BOOK_ID_ARG) { type = NavType.StringType }),
        ) {
            ShareScreen(navController = navController)
        }
        composable(Screen.Progress.route) {
            ProgressScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}

/** Pads a pre-redesign screen below the status bar. Removed as each screen migrates. */
@Composable
private fun LegacyScreen(content: @Composable () -> Unit) {
    Box(Modifier.statusBarsPadding()) { content() }
}
