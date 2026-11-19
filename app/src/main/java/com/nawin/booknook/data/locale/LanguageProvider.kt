package com.nawin.booknook.data.locale

import java.util.Locale

/**
 * The language the app is shown in, as an ISO 639-1 code ("es", "en").
 * Read on every call, so a change of app language applies to the next request.
 */
fun interface LanguageProvider {
    fun current(): String
}

/** Reads the process default locale, which follows the per-app language on Android 13+. */
class DeviceLanguageProvider : LanguageProvider {
    override fun current(): String = Locale.getDefault().language
}
