package com.nawin.booknook.di

import com.nawin.booknook.presentation.ui.bookdetail.BookDetailViewModel
import com.nawin.booknook.presentation.ui.calendar.CalendarViewModel
import com.nawin.booknook.presentation.ui.home.HomeViewModel
import com.nawin.booknook.presentation.ui.journal.JournalViewModel
import com.nawin.booknook.presentation.ui.library.LibraryViewModel
import com.nawin.booknook.presentation.ui.main.MainViewModel
import com.nawin.booknook.presentation.ui.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::BookDetailViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::JournalViewModel)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::SearchViewModel)
}

val appModules = listOf(
    databaseModule,
    networkModule,
    preferencesModule,
    repositoryModule,
    domainModule,
    viewModelModule
)
