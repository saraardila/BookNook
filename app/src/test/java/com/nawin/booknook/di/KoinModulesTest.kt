package com.nawin.booknook.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Koin resolves dependencies at runtime, so a missing definition would only
 * show up as a crash in the app. verify() checks every constructor in the
 * module graph at test time instead.
 */
@OptIn(KoinExperimentalAPI::class)
class KoinModulesTest {

    @Test
    fun `all Koin definitions can be resolved`() {
        module { includes(appModules) }.verify(
            extraTypes = listOf(
                // Provided by the platform at runtime, not by our modules
                Context::class,
                SavedStateHandle::class,
                // HttpClient is built in a lambda; verify() still inspects its constructor
                HttpClientEngine::class,
                HttpClientConfig::class,
                // Built in a lambda too, and null in builds without a Supabase project
                SupabaseClient::class,
            )
        )
    }
}
