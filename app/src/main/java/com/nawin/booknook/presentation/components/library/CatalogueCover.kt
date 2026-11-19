package com.nawin.booknook.presentation.components.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.presentation.components.BookCover
import com.nawin.booknook.presentation.theme.LibraryDimens

/** A book's cover with its library spine label in the bottom corner, as on a catalogue card. */
@Composable
fun CatalogueCover(
    book: Book,
    modifier: Modifier = Modifier,
    width: Dp = LibraryDimens.coverRowWidth,
    height: Dp = LibraryDimens.coverRowHeight,
) {
    val spine = remember(book.author, book.title) { SpineCode.from(book.author, book.title) }
    Box(modifier) {
        BookCover(
            title = book.title,
            author = book.author,
            coverUrl = book.coverUrl,
            modifier = Modifier.size(width, height),
            cornerRadius = LibraryDimens.radiusCover,
        )
        if (spine.author.isNotEmpty()) {
            SpineLabel(
                topLine = spine.author,
                bottomLine = spine.title,
                modifier = Modifier.align(Alignment.BottomStart).padding(LibraryDimens.spaceXxs),
            )
        }
    }
}
