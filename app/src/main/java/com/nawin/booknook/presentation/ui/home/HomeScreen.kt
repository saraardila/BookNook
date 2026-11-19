package com.nawin.booknook.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.presentation.components.BookCover
import com.nawin.booknook.presentation.components.library.Daisy
import com.nawin.booknook.presentation.components.library.DateStamp
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.LibraryChip
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.Polaroid
import com.nawin.booknook.presentation.components.library.ReadingProgress
import com.nawin.booknook.presentation.components.library.Shelf
import com.nawin.booknook.presentation.components.library.ShelfLabel
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.components.library.WashiTape
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.navigation.navigateToTopLevel
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Home tab: greeting, library summary, the book being read and recent finishes. */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Computed once per entry to Home; the greeting does not need to tick live.
    val dayPeriod = remember { DayPeriod.fromHour(LocalTime.now().hour) }

    HomeContent(
        state = uiState,
        dayPeriod = dayPeriod,
        onOpenBook = { bookId -> navController.navigate(Screen.BookDetail.createRoute(bookId)) },
        // Search and Shelves are tabs: open them as tabs, not on top of Home.
        onSearch = { navController.navigateToTopLevel(Screen.Search.route) },
        onSeeShelves = { navController.navigateToTopLevel(Screen.Library.route) },
        onOpenCalendar = { navController.navigate(Screen.Calendar.route) },
    )
}

/** Stateless Home layout, driven only by [state] so it can be previewed and tested. */
@Composable
fun HomeContent(
    state: HomeUiState,
    dayPeriod: DayPeriod,
    onOpenBook: (String) -> Unit,
    onSearch: () -> Unit,
    onSeeShelves: () -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Box {
            LaceHeader(height = LibraryDimens.headerHeight + statusBarTop)
            HomeHeader(
                dayPeriod = dayPeriod,
                state = state,
                onOpenCalendar = onOpenCalendar,
                modifier = Modifier.padding(
                    start = LibraryDimens.screenPadding,
                    end = LibraryDimens.screenPadding,
                    top = statusBarTop + LibraryDimens.spaceXl,
                ),
            )
        }

        Column(
            Modifier.padding(horizontal = LibraryDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.sectionGap),
        ) {
            Spacer(Modifier.height(LibraryDimens.spaceSm))
            val current = state.currentlyReading
            if (current != null) {
                LoanCard(book = current, onClick = { onOpenBook(current.id) })
            } else {
                EmptyLoanCard(onSearch = onSearch)
            }
            if (state.recentlyFinished.isNotEmpty()) {
                RecentlyFinishedShelf(
                    books = state.recentlyFinished,
                    onOpenBook = onOpenBook,
                    onSeeAll = onSeeShelves,
                )
            }
            Spacer(Modifier.height(LibraryDimens.spaceLg))
        }
    }
}

@Composable
private fun HomeHeader(
    dayPeriod: DayPeriod,
    state: HomeUiState,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(dayPeriod.greetingRes).lowercase(currentLocale()), style = type.hand, color = colors.stamp)
                Text(stringResource(R.string.home_my_library), style = type.title, color = colors.ink)
            }
            Sprig(stem = colors.primary, leaf = colors.primaryMid)
        }
        Spacer(Modifier.height(LibraryDimens.spaceSm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = libraryStats(state),
                style = type.chip,
                color = colors.inkMuted,
                modifier = Modifier.weight(1f),
            )
            LibraryChip(text = stringResource(R.string.calendar_title), onClick = onOpenCalendar)
        }
    }
}

@Composable
private fun libraryStats(state: HomeUiState): String = listOf(
    pluralStringResource(R.plurals.home_stat_books, state.totalBooks, state.totalBooks),
    pluralStringResource(R.plurals.home_stat_reading, state.booksReading, state.booksReading),
    pluralStringResource(R.plurals.home_stat_finished, state.booksFinished, state.booksFinished),
).joinToString(stringResource(R.string.home_stats_separator))

/** The book being read, shown as a library loan card with tape and a date stamp. */
@Composable
private fun LoanCard(book: Book, onClick: () -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Box {
        PaperCard(
            Modifier
                .fillMaxWidth()
                .clip(LibraryShapes.card)
                .clickable(role = Role.Button, onClick = onClick),
        ) {
            Column(Modifier.padding(LibraryDimens.spaceLg - LibraryDimens.spaceXxs)) {
                Text(stringResource(R.string.loan_card_label), style = type.label, color = colors.primary)
                Spacer(Modifier.height(LibraryDimens.spaceMd))
                Row(horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceLg)) {
                    Polaroid {
                        BookCover(
                            title = book.title,
                            author = book.author,
                            coverUrl = book.coverUrl,
                            modifier = Modifier.size(LibraryDimens.coverLoanWidth, LibraryDimens.coverLoanHeight),
                            cornerRadius = LibraryDimens.radiusCover,
                        )
                    }
                    Column(Modifier.weight(1f).padding(end = LibraryDimens.spaceXl)) {
                        Text(book.title, style = type.titleSmall, color = colors.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(book.author, style = type.body, color = colors.inkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        ProgressRow(book)
                    }
                }
            }
        }
        WashiTape(Modifier.offset(x = LibraryDimens.spaceXl + LibraryDimens.spaceXs, y = -LibraryDimens.laceScallop))
        book.startDate?.let { started ->
            DateStamp(
                label = stringResource(R.string.stamp_started),
                epochMillis = started,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = LibraryDimens.spaceSm, y = -LibraryDimens.spaceXl),
                rotation = DATE_STAMP_ROTATION,
                background = colors.paper,
            )
        }
    }
}

@Composable
private fun ProgressRow(book: Book) {
    val pages = book.pageCount ?: return
    if (pages <= 0) return
    val colors = LibraryTheme.colors
    val fraction = book.currentPage.toFloat() / pages
    Spacer(Modifier.height(LibraryDimens.spaceSm))
    ReadingProgress(fraction)
    Spacer(Modifier.height(LibraryDimens.spaceXs))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.home_page_progress, book.currentPage, pages),
            style = LibraryTheme.typography.chip,
            color = colors.inkMuted,
            modifier = Modifier.weight(1f),
        )
        Text(
            stringResource(R.string.home_progress_percent, (fraction * PERCENT).toInt()),
            style = LibraryTheme.typography.handSmall,
            color = colors.stamp,
        )
    }
}

private const val PERCENT = 100

private const val DATE_STAMP_ROTATION = 12f

@Composable
private fun EmptyLoanCard(onSearch: () -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    PaperCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(LibraryDimens.spaceXl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
        ) {
            Daisy(size = LibraryDimens.navIconContainer)
            Text(stringResource(R.string.home_no_book_title), style = type.titleSmall, color = colors.ink, textAlign = TextAlign.Center)
            Text(stringResource(R.string.home_no_book_subtitle), style = type.body, color = colors.inkMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(LibraryDimens.spaceXs))
            PillButton(stringResource(R.string.home_search_book), onClick = onSearch)
        }
    }
}

/** Recently finished books standing on a shelf, with a link to all shelves. */
@Composable
private fun RecentlyFinishedShelf(
    books: List<Book>,
    onOpenBook: (String) -> Unit,
    onSeeAll: () -> Unit,
) {
    val colors = LibraryTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShelfLabel(stringResource(R.string.home_recently_finished))
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.home_see_all),
                style = LibraryTheme.typography.chip,
                color = colors.primary,
                modifier = Modifier
                    .heightIn(min = LibraryDimens.touchTarget)
                    .clickable(role = Role.Button, onClick = onSeeAll)
                    .padding(horizontal = LibraryDimens.spaceSm, vertical = LibraryDimens.spaceMd),
            )
        }
        Column {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = LibraryDimens.spaceSm),
                horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
                verticalAlignment = Alignment.Bottom,
            ) {
                books.take(SHELF_CAPACITY).forEach { book ->
                    BookCover(
                        title = book.title,
                        author = book.author,
                        coverUrl = book.coverUrl,
                        modifier = Modifier
                            .width(LibraryDimens.coverShelfWidth)
                            .height(LibraryDimens.coverShelfHeight)
                            .semantics { contentDescription = book.title }
                            .clickable(role = Role.Button) { onOpenBook(book.id) },
                        cornerRadius = LibraryDimens.radiusCover,
                    )
                }
            }
            Shelf()
        }
    }
}

/** How many covers fit on the Home shelf at phone width. */
private const val SHELF_CAPACITY = 3

@Composable
private fun currentLocale() = LocalConfiguration.current.locales[0]

// region Previews

private val previewBook = Book(
    id = "preview",
    title = "Throne of Glass",
    author = "Sarah J. Maas",
    coverUrl = null,
    description = null,
    publishYear = 2012,
    pageCount = 404,
    isbn = null,
    status = ReadingStatus.READING,
    currentPage = 212,
    startDate = LocalDate.of(2026, 11, 2).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
)

private val previewState = HomeUiState(
    currentlyReading = previewBook,
    recentlyFinished = listOf(
        previewBook.copy(id = "1", title = "Heir of Fire", status = ReadingStatus.FINISHED),
        previewBook.copy(id = "2", title = "Fourth Wing", author = "Rebecca Yarros", status = ReadingStatus.FINISHED),
        previewBook.copy(id = "3", title = "A Court of Mist and Fury", status = ReadingStatus.FINISHED),
    ),
    totalBooks = 27,
    booksFinished = 9,
    booksReading = 2,
)

@Preview(name = "Home · English", locale = "en", widthDp = 390, heightDp = 844)
@Preview(name = "Home · Spanish", locale = "es", widthDp = 390, heightDp = 844)
@Composable
private fun HomePreview() = LibraryTheme {
    HomeContent(previewState, DayPeriod.Afternoon, onOpenBook = {}, onSearch = {}, onSeeShelves = {}, onOpenCalendar = {})
}

@Preview(name = "Home · empty", locale = "es", widthDp = 390, heightDp = 844)
@Composable
private fun HomeEmptyPreview() = LibraryTheme {
    HomeContent(HomeUiState(), DayPeriod.Morning, onOpenBook = {}, onSearch = {}, onSeeShelves = {}, onOpenCalendar = {})
}

// endregion
