package com.nawin.booknook.di

import com.nawin.booknook.presentation.ui.bookdetail.BookDetailViewModel
import com.nawin.booknook.presentation.ui.calendar.CalendarViewModel
import com.nawin.booknook.presentation.ui.home.HomeViewModel
import com.nawin.booknook.presentation.ui.newstamp.NewStampViewModel
import com.nawin.booknook.presentation.ui.progress.ProgressViewModel
import com.nawin.booknook.presentation.ui.readingcard.ReadingCardViewModel
import com.nawin.booknook.presentation.ui.journal.JournalViewModel
import com.nawin.booknook.presentation.ui.library.LibraryViewModel
import com.nawin.booknook.presentation.ui.main.MainViewModel
import com.nawin.booknook.presentation.ui.search.SearchViewModel
import com.nawin.booknook.presentation.ui.onboarding.OnboardingViewModel
import com.nawin.booknook.presentation.ui.settings.SettingsViewModel
import com.nawin.booknook.presentation.ui.share.ShareViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::BookDetailViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ProgressViewModel)
    viewModelOf(::ReadingCardViewModel)
    viewModelOf(::NewStampViewModel)
    viewModelOf(::JournalViewModel)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::ShareViewModel)
}

val appModules = listOf(
    databaseModule,
    networkModule,
    preferencesModule,
    repositoryModule,
    domainModule,
    servicesModule,
    viewModelModule
)
