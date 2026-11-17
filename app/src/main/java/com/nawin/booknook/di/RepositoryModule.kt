package com.nawin.booknook.di

import com.nawin.booknook.data.repository.BookRepositoryImpl
import com.nawin.booknook.domain.repository.BookRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::BookRepositoryImpl) bind BookRepository::class
}
