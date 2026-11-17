package com.nawin.booknook

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.nawin.booknook.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BookNookApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BookNookApplication)
            modules(appModules)
        }
    }

    /**
     * Declared explicitly rather than left to Coil's classpath detection, which
     * is JVM-only: this is the same setup iOS will need.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
}
