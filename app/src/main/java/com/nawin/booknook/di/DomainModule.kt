package com.nawin.booknook.di

import com.nawin.booknook.domain.usecase.AddBookToLibraryUseCase
import com.nawin.booknook.domain.usecase.AddNoteUseCase
import com.nawin.booknook.domain.usecase.GetAccountUseCase
import com.nawin.booknook.domain.usecase.GetBookByIdUseCase
import com.nawin.booknook.domain.usecase.GetDailyQuoteUseCase
import com.nawin.booknook.domain.usecase.GetJournalUseCase
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import com.nawin.booknook.domain.usecase.GetNotesUseCase
import com.nawin.booknook.domain.usecase.GetPagesPerDayUseCase
import com.nawin.booknook.domain.usecase.GetReadingCardUseCase
import com.nawin.booknook.domain.usecase.GetReadingStreakUseCase
import com.nawin.booknook.domain.usecase.GetShelfLicencesUseCase
import com.nawin.booknook.domain.usecase.SearchBooksUseCase
import com.nawin.booknook.domain.usecase.SignInUseCase
import com.nawin.booknook.domain.usecase.SignOutUseCase
import com.nawin.booknook.domain.usecase.SignUpUseCase
import com.nawin.booknook.domain.usecase.UpdateBookUseCase
import com.nawin.booknook.domain.usecase.UpdateReadingProgressUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::AddBookToLibraryUseCase)
    factoryOf(::AddNoteUseCase)
    factoryOf(::GetBookByIdUseCase)
    factoryOf(::GetJournalUseCase)
    factoryOf(::GetMyLibraryUseCase)
    factoryOf(::GetNotesUseCase)
    factoryOf(::GetReadingStreakUseCase)
    factoryOf(::GetShelfLicencesUseCase)
    factoryOf(::GetReadingCardUseCase)
    factoryOf(::SearchBooksUseCase)
    factoryOf(::GetDailyQuoteUseCase)
    factoryOf(::GetAccountUseCase)
    factoryOf(::GetPagesPerDayUseCase)
    factoryOf(::SignUpUseCase)
    factoryOf(::SignInUseCase)
    factoryOf(::SignOutUseCase)
    factoryOf(::UpdateBookUseCase)
    factoryOf(::UpdateReadingProgressUseCase)
}
