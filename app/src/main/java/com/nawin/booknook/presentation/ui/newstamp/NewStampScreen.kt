package com.nawin.booknook.presentation.ui.newstamp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.CardStamp
import com.nawin.booknook.domain.model.ReadingCard
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.model.ShelfLicence
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.presentation.components.BookCover
import com.nawin.booknook.presentation.components.library.Daisy
import com.nawin.booknook.presentation.components.library.DateStamp
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.RATING_MAX
import com.nawin.booknook.presentation.components.library.RatingInput
import com.nawin.booknook.presentation.components.library.Rosette
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.components.library.WashiTape
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.theme.code
import com.nawin.booknook.presentation.theme.nameRes
import com.nawin.booknook.presentation.theme.palette
import org.koin.compose.viewmodel.koinViewModel

/**
 * Shown once a book is marked as finished: its new stamp on the reading card, how the
 * card and the shelf licence are going, and a quick rating.
 */
@Composable
fun NewStampScreen(
    navController: NavController,
    viewModel: NewStampViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NewStampContent(
        state = state,
        onRatingChange = viewModel::updateRating,
        onSeeCard = {
            // The card replaces this screen, so back from it returns to the book.
            navController.navigate(Screen.ReadingCard.route) {
                popUpTo(Screen.NewStamp.route) { inclusive = true }
            }
        },
        onBackToBook = { navController.popBackStack() },
    )
}

/** Stateless layout of [NewStampScreen]. */
@Composable
fun NewStampContent(
    state: NewStampUiState,
    onRatingChange: (Float) -> Unit,
    onSeeCard: () -> Unit,
    onBackToBook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val book = state.book
    val card = state.card
    val stamp = book?.let { b -> card?.stamps?.lastOrNull { it.bookId == b.id } }

    Box(modifier.fillMaxSize().background(colors.primaryLight)) {
        Daisy(Modifier.padding(start = LibraryDimens.spaceXl, top = LibraryDimens.headerHeightCompact).align(Alignment.TopStart))
        Sprig(Modifier.padding(end = LibraryDimens.spaceLg, top = LibraryDimens.headerHeight).align(Alignment.TopEnd))

        if (book == null || card == null) {
            ShelfLoader(Modifier.align(Alignment.Center))
            return@Box
        }

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = LibraryDimens.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(LibraryDimens.sectionGap))
                Text(stringResource(R.string.new_stamp_hint), style = type.hand, color = colors.stamp)
                Text(stringResource(R.string.new_stamp_title), style = type.heading, color = colors.ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(LibraryDimens.sectionGap))
                StampedBook(book, stamp, card)
                Spacer(Modifier.height(LibraryDimens.spaceLg))
                LicenceNote(state.licence)
                Spacer(Modifier.height(LibraryDimens.spaceLg))
                Text(stringResource(R.string.new_stamp_rate), style = type.label, color = colors.inkMuted)
                RatingInput(
                    rating = book.rating,
                    onRatingChange = onRatingChange,
                    starDescription = { stars -> pluralStringResource(R.plurals.cd_rating, RATING_MAX, stars, RATING_MAX) },
                )
                Spacer(Modifier.height(LibraryDimens.spaceLg))
            }
            Row(
                Modifier.fillMaxWidth().padding(LibraryDimens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
            ) {
                PillButton(stringResource(R.string.new_stamp_back_to_book), onClick = onBackToBook, primary = false, modifier = Modifier.weight(1f))
                PillButton(stringResource(R.string.new_stamp_see_card), onClick = onSeeCard, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** The cover with the fresh stamp pressed on its corner, and the card count below. */
@Composable
private fun StampedBook(book: Book, stamp: CardStamp?, card: ReadingCard) {
    val colors = LibraryTheme.colors
    Box {
        PaperCard(cornerRadius = LibraryDimens.radiusSheet) {
            Column(
                Modifier.padding(horizontal = LibraryDimens.sectionGap, vertical = LibraryDimens.spaceLg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    BookCover(
                        title = book.title,
                        author = book.author,
                        coverUrl = book.coverUrl,
                        modifier = Modifier
                            .padding(end = LibraryDimens.spaceXl, bottom = LibraryDimens.spaceLg)
                            .size(LibraryDimens.coverDetailWidth, LibraryDimens.coverDetailHeight),
                        cornerRadius = LibraryDimens.radiusCover,
                    )
                    stamp?.let {
                        DateStamp(
                            label = stringResource(R.string.stamp_read),
                            epochMillis = it.date,
                            modifier = Modifier.align(Alignment.BottomEnd).offset(x = LibraryDimens.spaceXl),
                            size = LibraryDimens.newStampSize,
                            rotation = NEW_STAMP_ROTATION,
                            background = colors.paper.copy(alpha = STAMP_BACKGROUND_ALPHA),
                        )
                    }
                }
                Spacer(Modifier.height(LibraryDimens.spaceSm))
                Text(
                    stringResource(R.string.new_stamp_card_count, card.stamps.size, ReadingCard.SLOTS),
                    style = LibraryTheme.typography.button,
                    color = colors.ink,
                )
            }
        }
        WashiTape(Modifier.offset(x = LibraryDimens.spaceXl + LibraryDimens.spaceSm, y = -LibraryDimens.spaceSm))
    }
}

/** How the book's shelf licence is going, or a nudge to file it under a shelf. */
@Composable
private fun LicenceNote(licence: ShelfLicence?) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    PaperCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
        ) {
            if (licence == null) {
                Rosette(dashed = true, size = LibraryDimens.stampSizeSmall) {
                    Text(stringResource(R.string.progress_licence_unknown), style = type.hand, color = colors.inkMuted)
                }
                Text(stringResource(R.string.new_stamp_no_shelf), style = type.body, color = colors.inkMuted)
            } else {
                val palette = licence.shelf.palette
                val name = stringResource(licence.shelf.nameRes)
                if (licence.earned) {
                    Rosette(fill = palette.primary, size = LibraryDimens.stampSizeSmall, ring = colors.onPrimary) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(LibraryDimens.streakDayCheck))
                    }
                } else {
                    Rosette(fill = palette.tint, size = LibraryDimens.stampSizeSmall) {
                        Text(licence.shelf.code, style = type.label, color = palette.primary)
                    }
                }
                Column {
                    Text(
                        stringResource(R.string.new_stamp_licence, name, licence.progress, ShelfLicence.BOOKS_PER_LICENCE),
                        style = type.button,
                        color = colors.ink,
                    )
                    val left = ShelfLicence.BOOKS_PER_LICENCE - licence.progress
                    Text(
                        if (licence.earned) stringResource(R.string.new_stamp_licence_earned)
                        else pluralStringResource(R.plurals.new_stamp_licence_left, left, left),
                        style = type.body,
                        color = colors.inkMuted,
                    )
                }
            }
        }
    }
}

private const val NEW_STAMP_ROTATION = -14f
private const val STAMP_BACKGROUND_ALPHA = 0.9f

// region Previews

@Preview(name = "New stamp · Spanish", locale = "es", widthDp = 390, heightDp = 844)
@Preview(name = "New stamp · English", locale = "en", widthDp = 390, heightDp = 844)
@Composable
private fun NewStampPreview() = LibraryTheme {
    val book = Book(
        id = "1", title = "A Court of Mist and Fury", author = "Sarah J. Maas", coverUrl = null, description = null,
        publishYear = 2016, pageCount = 626, isbn = null, status = ReadingStatus.FINISHED, rating = 4f,
        finishDate = 1_795_000_000_000, shelf = ShelfTheme.ROMANTASY,
    )
    NewStampContent(
        NewStampUiState(
            book = book,
            card = ReadingCard(1, List(7) { CardStamp(if (it == 6) "1" else "x$it", "", 1_795_000_000_000, false) }),
            licence = ShelfLicence(ShelfTheme.ROMANTASY, 4),
        ),
        onRatingChange = {}, onSeeCard = {}, onBackToBook = {},
    )
}

// endregion
