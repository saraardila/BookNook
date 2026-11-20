package com.nawin.booknook.di

import com.nawin.booknook.domain.usecase.AddBookToLibraryUseCase
import com.nawin.booknook.domain.usecase.AddNoteUseCase
import com.nawin.booknook.domain.usecase.GetBookByIdUseCase
import com.nawin.booknook.domain.usecase.GetJournalUseCase
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import com.nawin.booknook.domain.usecase.GetNotesUseCase
import com.nawin.booknook.domain.usecase.GetReadingCardUseCase
import com.nawin.booknook.domain.usecase.GetReadingStreakUseCase
import com.nawin.booknook.domain.usecase.GetShelfLicencesUseCase
import com.nawin.booknook.domain.usecase.SearchBooksUseCase
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
    factoryOf(::UpdateBookUseCase)
    factoryOf(::UpdateReadingProgressUseCase)
}
