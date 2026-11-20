package com.nawin.booknook.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.nawin.booknook.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "booknook_db")
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    factory { get<AppDatabase>().bookDao() }
    factory { get<AppDatabase>().bookNoteDao() }
    factory { get<AppDatabase>().readingLogDao() }
}
