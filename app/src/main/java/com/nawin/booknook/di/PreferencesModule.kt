package com.nawin.booknook.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.nawin.booknook.data.local.PreferencesDataStore
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** Same path the Context.preferencesDataStore delegate used, so existing settings are kept. */
private const val PREFERENCES_FILE = "datastore/booknook_prefs.preferences_pb"

val preferencesModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { androidContext().filesDir.resolve(PREFERENCES_FILE).absolutePath.toPath() }
        )
    }
    singleOf(::PreferencesDataStore)
}
