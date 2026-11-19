package com.nawin.booknook.data.locale

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * The language the app is shown in, as an ISO 639-1 code ("es", "en").
 * Read on every call, so a change of app language applies to the next request.
 */
fun interface LanguageProvider {
    fun current(): String
}

/**
 * The language chosen in Settings (stored by AppCompat on every Android
 * version), or the device language when the reader has not chosen one.
 */
class AppLanguageProvider : LanguageProvider {
    override fun current(): String =
        AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language
}
