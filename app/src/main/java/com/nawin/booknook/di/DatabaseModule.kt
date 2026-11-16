package com.nawin.booknook.di

import androidx.room.Room
import com.nawin.booknook.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "booknook_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    factory { get<AppDatabase>().bookDao() }
    factory { get<AppDatabase>().bookNoteDao() }
}
