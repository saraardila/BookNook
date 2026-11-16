package com.nawin.booknook

import android.app.Application
import com.nawin.booknook.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BookNookApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BookNookApplication)
            modules(appModules)
        }
    }
}
