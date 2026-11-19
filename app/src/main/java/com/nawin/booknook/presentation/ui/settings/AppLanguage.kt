package com.nawin.booknook.presentation.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Languages the app is translated into. [tag] is the BCP 47 tag handed to
 * AppCompat; the names are shown in their own language, as is customary.
 */
enum class AppLanguage(val tag: String, val language: String) {
    SPANISH("es-ES", "es"),
    ENGLISH("en", "en");

    companion object {
        /** The language chosen in the app, or the device one when none was chosen. */
        fun current(): AppLanguage? {
            val locale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
            return entries.firstOrNull { it.language == locale.language }
        }

        /**
         * Switches the app language. AppCompat stores the choice (on Android 13+
         * in the system's per-app language setting) and recreates the activity.
         */
        fun apply(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        }
    }
}
