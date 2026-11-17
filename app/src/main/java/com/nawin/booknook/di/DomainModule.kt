package com.nawin.booknook.di

import com.nawin.booknook.domain.usecase.AddBookToLibraryUseCase
import com.nawin.booknook.domain.usecase.AddNoteUseCase
import com.nawin.booknook.domain.usecase.GetBookByIdUseCase
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import com.nawin.booknook.domain.usecase.GetNotesUseCase
import com.nawin.booknook.domain.usecase.SearchBooksUseCase
import com.nawin.booknook.domain.usecase.UpdateBookUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::AddBookToLibraryUseCase)
    factoryOf(::AddNoteUseCase)
    factoryOf(::GetBookByIdUseCase)
    factoryOf(::GetMyLibraryUseCase)
    factoryOf(::GetNotesUseCase)
    factoryOf(::SearchBooksUseCase)
    factoryOf(::UpdateBookUseCase)
}
