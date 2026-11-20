package com.nawin.booknook.presentation.theme

import androidx.annotation.StringRes
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.ShelfTheme

/** Name of the shelf as shown to the reader. */
@get:StringRes
val ShelfTheme.nameRes: Int
    get() = when (this) {
        ShelfTheme.STANDARD -> R.string.theme_standard
        ShelfTheme.ROMANTASY -> R.string.theme_romantasy
        ShelfTheme.WITCHY -> R.string.theme_witchy
        ShelfTheme.COWBOY -> R.string.theme_cowboy
        ShelfTheme.ROMANCE -> R.string.theme_romance
        ShelfTheme.THRILLER -> R.string.theme_thriller
    }

/**
 * Three-letter catalogue code printed on labels, stamps and the library card
 * ("ESTANTE · ROM"). Like the spine codes, it is the same in every language.
 */
val ShelfTheme.code: String
    get() = when (this) {
        ShelfTheme.STANDARD -> "STD"
        ShelfTheme.ROMANTASY -> "ROM"
        ShelfTheme.WITCHY -> "WIT"
        ShelfTheme.COWBOY -> "COW"
        ShelfTheme.ROMANCE -> "RMC"
        ShelfTheme.THRILLER -> "THR"
    }
