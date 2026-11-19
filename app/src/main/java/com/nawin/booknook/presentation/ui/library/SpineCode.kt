package com.nawin.booknook.presentation.ui.library

import java.text.Normalizer
import java.util.Locale

/**
 * The two lines of a book's spine label, following the format used by
 * Spanish public libraries: the first three letters of the author's surname
 * in capitals, over the first letter of the title (skipping a leading
 * article) in lower case. E.g. Sarah J. Maas, "The Assassin's Blade" → MAA / a.
 *
 * Once books belong to themed shelves, the shelf code will take the top line.
 */
data class SpineCode(val author: String, val title: String) {

    companion object {
        /** Builds the code from raw [author] and [title]; empty lines when there is nothing usable. */
        fun from(author: String, title: String): SpineCode {
            val surname = author.trim().split(WHITESPACE).lastOrNull().orEmpty()
            val authorCode = surname.lettersOnly().take(AUTHOR_CODE_LENGTH).uppercase(Locale.ROOT)

            val words = title.trim().split(WHITESPACE).filter { it.isNotEmpty() }
            val significant = words.dropWhile { it.lowercase(Locale.ROOT) in LEADING_ARTICLES }.ifEmpty { words }
            val titleCode = significant.joinToString("").lettersOnly().take(1).lowercase(Locale.ROOT)

            return SpineCode(authorCode, titleCode)
        }

        private fun String.lettersOnly(): String =
            Normalizer.normalize(this, Normalizer.Form.NFD).filter { it.isLetter() }

        private val WHITESPACE = Regex("\\s+")
        private const val AUTHOR_CODE_LENGTH = 3

        /** Articles skipped at the start of a title, in the languages the app supports. */
        private val LEADING_ARTICLES = setOf("the", "a", "an", "el", "la", "los", "las", "lo", "un", "una", "unos", "unas")
    }
}
