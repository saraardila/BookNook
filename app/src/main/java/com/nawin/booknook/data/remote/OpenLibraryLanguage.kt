package com.nawin.booknook.data.remote

/**
 * Open Library tags editions with MARC language codes, which differ from
 * ISO 639-2/T in places (French is "fre", not "fra"), so the app's languages
 * are mapped explicitly instead of through `Locale.isO3Language`.
 */
object OpenLibraryLanguage {

    private val MARC_CODES = mapOf(
        "en" to "eng",
        "es" to "spa",
    )

    /** MARC code for an ISO 639-1 [language], or null if the app does not support it. */
    fun marcCode(language: String): String? = MARC_CODES[language.lowercase()]
}
