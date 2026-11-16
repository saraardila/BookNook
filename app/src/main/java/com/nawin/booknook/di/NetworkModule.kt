package com.nawin.booknook.di

import com.nawin.booknook.BuildConfig
import com.nawin.booknook.data.remote.api.OpenLibraryApi
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private const val OPEN_LIBRARY_URL = "https://openlibrary.org/"

/** Shared client setup, so tests can apply it to a MockEngine. */
fun HttpClientConfig<*>.openLibraryDefaults() {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    defaultRequest { url(OPEN_LIBRARY_URL) }
}

val networkModule = module {
    single {
        HttpClient(OkHttp) {
            openLibraryDefaults()
            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = Logger.ANDROID
                    level = LogLevel.HEADERS
                }
            }
        }
    }
    singleOf(::OpenLibraryApi)
}
