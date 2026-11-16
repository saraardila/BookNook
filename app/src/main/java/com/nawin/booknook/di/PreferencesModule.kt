package com.nawin.booknook.di

import com.nawin.booknook.data.local.PreferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesModule = module {
    single { PreferencesDataStore(androidContext()) }
}
