package com.nawin.booknook.presentation.navigation

sealed class Screen(val route: String) {
    object Home       : Screen("home")
    object Search     : Screen("search")
    object Library    : Screen("library")
    object Calendar   : Screen("calendar")
    object Journal    : Screen("journal")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: String) = "book_detail/$bookId"
    }
}