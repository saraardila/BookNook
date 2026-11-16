package com.nawin.booknook.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
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
            // Provided by the platform at runtime, not by our modules
            extraTypes = listOf(Context::class, SavedStateHandle::class)
        )
    }
}
