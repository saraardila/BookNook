package com.nawin.booknook.presentation.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.JournalEntry
import com.nawin.booknook.domain.model.ReadingMood
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.presentation.components.BookCover
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.LibraryChip
import com.nawin.booknook.presentation.components.library.NoticeCard
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.Polaroid
import com.nawin.booknook.presentation.components.library.RATING_MAX
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.components.library.StarRating
import com.nawin.booknook.presentation.components.library.WashiTape
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.ui.note.NoteSheet
import com.nawin.booknook.presentation.ui.note.labelRes
import org.koin.compose.viewmodel.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** "My diary": reviews, notes, loose thoughts and put-down books, one month at a time. */
@Composable
fun JournalScreen(
    navController: NavController,
    viewModel: JournalViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.showThoughtSheet) {
        NoteSheet(
            bookTitle = null,
            page = null,
            onDismiss = { viewModel.showThoughtSheet(false) },
            onSave = { _, mood, content -> viewModel.addThought(mood, content) },
        )
    }
    JournalContent(
        state = state,
        onSelectMonth = viewModel::selectMonth,
        onOpenBook = { navController.navigate(Screen.BookDetail.createRoute(it)) },
        onAddEntry = { viewModel.showThoughtSheet(true) },
    )
}

/** Stateless layout of [JournalScreen]. */
@Composable
fun JournalContent(
    state: JournalUiState,
    onSelectMonth: (YearMonth) -> Unit,
    onOpenBook: (String) -> Unit,
    onAddEntry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            // Room for the "+ Entry" button so it never covers the last entry.
            contentPadding = PaddingValues(bottom = LibraryDimens.buttonMinHeight + LibraryDimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceLg),
        ) {
            item(key = KEY_HEADER) {
                Box {
                    LaceHeader(height = LibraryDimens.headerHeightCompact + statusBarTop, color = colors.accentLight)
                    Row(
                        Modifier.fillMaxWidth().padding(
                            start = LibraryDimens.screenPadding,
                            end = LibraryDimens.screenPadding,
                            top = statusBarTop + LibraryDimens.spaceXl,
                        ),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.journal_tagline), style = type.hand, color = colors.accentDeep)
                            Text(stringResource(R.string.journal_title), style = type.title, color = colors.ink)
                        }
                        Sprig(stem = colors.accentDeep, leaf = colors.paper)
                    }
                }
            }
            when {
                state.isLoading -> item(key = KEY_LOADING) { ShelfLoader(Modifier.fillMaxWidth().padding(top = LibraryDimens.sectionGap)) }
                state.months.isEmpty() -> item(key = KEY_EMPTY) {
                    NoticeCard(
                        title = stringResource(R.string.journal_empty_title),
                        subtitle = stringResource(R.string.journal_empty_subtitle),
                        modifier = Modifier.padding(horizontal = LibraryDimens.screenPadding),
                    )
                }
                else -> {
                    item(key = KEY_MONTHS) { MonthRow(state, onSelectMonth) }
                    items(state.entries, key = { it.key }) { entry ->
                        val entryModifier = Modifier.padding(horizontal = LibraryDimens.screenPadding)
                        when (entry) {
                            is JournalEntry.Finished -> FinishedEntry(entry, { onOpenBook(entry.book.id) }, entryModifier)
                            is JournalEntry.Note -> NoteEntry(entry, entry.book?.let { book -> { onOpenBook(book.id) } }, entryModifier)
                            is JournalEntry.PutDown -> PutDownEntry(entry, { onOpenBook(entry.book.id) }, entryModifier)
                        }
                    }
                }
            }
        }
        PillButton(
            stringResource(R.string.journal_add_entry),
            onClick = onAddEntry,
            modifier = Modifier.align(Alignment.BottomEnd).padding(LibraryDimens.screenPadding),
        )
    }
}

/** One chip per month with entries, newest first; the year shows only for past years. */
@Composable
private fun MonthRow(state: JournalUiState, onSelect: (YearMonth) -> Unit) {
    val locale = currentLocale()
    LazyRow(
        contentPadding = PaddingValues(horizontal = LibraryDimens.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
        modifier = Modifier.padding(top = LibraryDimens.spaceSm),
    ) {
        items(state.months, key = { it.toString() }) { month ->
            LibraryChip(
                monthName(month, state.currentMonth, locale),
                selected = month == state.selectedMonth,
                onClick = { onSelect(month) },
            )
        }
    }
}

/** A finished book: the cover on a polaroid, the rating and the review in handwriting. */
@Composable
private fun FinishedEntry(entry: JournalEntry.Finished, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val book = entry.book
    Box(modifier.padding(top = LibraryDimens.spaceSm)) {
        PaperCard(Modifier.fillMaxWidth().rotate(CARD_TILT_LEFT)) {
            Row(
                Modifier.clip(LibraryShapes.card).clickable(onClick = onClick).padding(LibraryDimens.spaceMd + LibraryDimens.spaceXxs),
                horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
            ) {
                Polaroid {
                    BookCover(
                        title = book.title,
                        author = book.author,
                        coverUrl = book.coverUrl,
                        modifier = Modifier.size(LibraryDimens.coverRowWidth, LibraryDimens.coverRowHeight),
                        cornerRadius = LibraryDimens.radiusCover,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EntryLabel(
                            entry.date,
                            stringResource(if (entry.hasReview) R.string.journal_label_review else R.string.journal_label_finished),
                            Modifier.weight(1f),
                        )
                        if (book.rating > 0f) {
                            val stars = book.rating.toInt()
                            StarRating(book.rating, pluralStringResource(R.plurals.cd_rating, RATING_MAX, stars, RATING_MAX))
                        }
                    }
                    Text(book.title, style = type.titleSmall, color = colors.ink)
                    book.review?.takeIf { it.isNotBlank() }?.let { Text(it, style = type.hand, color = colors.ink) }
                }
            }
        }
        WashiTape(Modifier.offset(x = LibraryDimens.spaceXl, y = -LibraryDimens.spaceSm))
    }
}

/** A note on a book, or a loose thought, on a tinted card with its mood. */
@Composable
private fun NoteEntry(entry: JournalEntry.Note, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val note = entry.note
    val book = entry.book
    PaperCard(modifier.fillMaxWidth().rotate(CARD_TILT_RIGHT), color = colors.primaryLight) {
        val clickable = if (onClick != null) Modifier.clip(LibraryShapes.card).clickable(onClick = onClick) else Modifier
        Column(
            clickable.fillMaxWidth().padding(horizontal = LibraryDimens.spaceLg, vertical = LibraryDimens.spaceMd + LibraryDimens.spaceXxs),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs),
        ) {
            EntryLabel(note.createdAt, stringResource(if (book == null) R.string.journal_label_thought else R.string.journal_label_note))
            if (book != null) {
                val where = listOfNotNull(book.title, note.page?.let { stringResource(R.string.detail_note_page, it) })
                Text(where.joinToString(META_SEPARATOR), style = type.caption, color = colors.inkMuted)
            }
            if (note.content.isNotEmpty()) {
                Text(note.content, style = if (book == null) type.emphasis else type.hand, color = colors.ink)
            }
            note.mood?.let { LibraryChip(stringResource(it.labelRes)) }
        }
    }
}

/** A book she put down: the page she left it on and, if she wrote one, why. */
@Composable
private fun PutDownEntry(entry: JournalEntry.PutDown, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val book = entry.book
    PaperCard(modifier.fillMaxWidth()) {
        Row(
            Modifier.clip(LibraryShapes.card).clickable(onClick = onClick).fillMaxWidth().padding(LibraryDimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCover(
                title = book.title,
                author = book.author,
                coverUrl = book.coverUrl,
                modifier = Modifier.size(LibraryDimens.coverRowWidth, LibraryDimens.coverRowHeight),
                cornerRadius = LibraryDimens.radiusCover,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXxs)) {
                EntryLabel(entry.date, stringResource(R.string.journal_label_put_down))
                Text(book.title, style = type.titleSmall, color = colors.ink)
                Text(
                    if (book.currentPage > 0) stringResource(R.string.journal_put_down_at, book.currentPage)
                    else stringResource(R.string.journal_put_down_unread),
                    style = type.caption,
                    color = colors.inkMuted,
                )
                book.review?.takeIf { it.isNotBlank() }?.let { Text(it, style = type.handSmall, color = colors.accentDeep) }
            }
        }
    }
}

/** "28 OCT 2026 · REVIEW": the date and what kind of entry it is. */
@Composable
private fun EntryLabel(epochMillis: Long, kind: String, modifier: Modifier = Modifier) {
    val locale = currentLocale()
    val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    Text(
        (date + META_SEPARATOR + kind).uppercase(locale),
        style = LibraryTheme.typography.label,
        color = LibraryTheme.colors.primary,
        modifier = modifier,
    )
}

private fun monthName(month: YearMonth, current: YearMonth?, locale: Locale): String {
    val name = month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }
    return if (month.year == current?.year) name else "$name ${month.year}"
}

@Composable
private fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

private const val KEY_HEADER = "header"
private const val KEY_LOADING = "loading"
private const val KEY_EMPTY = "empty"
private const val KEY_MONTHS = "months"
private const val META_SEPARATOR = " · "
private const val CARD_TILT_LEFT = -0.8f
private const val CARD_TILT_RIGHT = 0.8f

// region Previews

@Preview(name = "Journal · Spanish", locale = "es", widthDp = 390, heightDp = 900)
@Preview(name = "Journal · English", locale = "en", widthDp = 390, heightDp = 900)
@Composable
private fun JournalPreview() = LibraryTheme {
    val zone = ZoneId.systemDefault()
    fun day(month: Int, dayOfMonth: Int) = LocalDate.of(2026, month, dayOfMonth).atStartOfDay(zone).toInstant().toEpochMilli()
    val acotar = Book(
        "1", "A Court of Mist and Fury", "Sarah J. Maas", null, null, 2016, 626, null, ReadingStatus.FINISHED,
        rating = 4f, review = "Empecé por curiosidad y acabé a las 3 de la mañana. Necesito el segundo YA.",
    )
    val crows = Book("2", "Six of Crows", "Leigh Bardugo", null, null, 2015, 465, null, ReadingStatus.DNF, currentPage = 140, review = "lo retomaré en invierno")
    JournalContent(
        JournalUiState(
            isLoading = false,
            months = listOf(YearMonth.of(2026, 11), YearMonth.of(2026, 10), YearMonth.of(2025, 9)),
            selectedMonth = YearMonth.of(2026, 11),
            currentMonth = YearMonth.of(2026, 11),
            entries = listOf(
                JournalEntry.Finished(acotar, day(11, 18)),
                JournalEntry.Note(BookNote(1, null, null, "Hay libros que se leen y libros que se viven.", null, ReadingMood.SMITTEN, day(11, 17)), null),
                JournalEntry.PutDown(crows, day(11, 10)),
            ),
        ),
        onSelectMonth = {}, onOpenBook = {}, onAddEntry = {},
    )
}

// endregion
