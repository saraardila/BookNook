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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.DailyQuote
import com.nawin.booknook.domain.model.HomeBlock
import com.nawin.booknook.domain.model.ShelfLicence
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.model.ReadingStreak
import com.nawin.booknook.presentation.components.BookCover
import com.nawin.booknook.presentation.components.library.DateStamp
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.LibraryChip
import com.nawin.booknook.presentation.components.library.NoticeCard
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.Polaroid
import com.nawin.booknook.presentation.components.library.ReadingProgress
import com.nawin.booknook.presentation.components.library.Shelf
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.components.library.ShelfLabel
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.components.library.StreakWeek
import com.nawin.booknook.presentation.components.library.WashiTape
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.navigation.navigateToTopLevel
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.ui.progress.Licences
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Home tab: greeting and library summary, then the blocks she chose in her order (P10, P11). */
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
        onOpenProgress = { navController.navigate(Screen.Progress.route) },
        onEditHome = { navController.navigate(Screen.EditHome.route) },
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
    onOpenProgress: () -> Unit,
    onEditHome: () -> Unit,
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

        // Outside the blocks' column so it sits close to the first block instead of a section gap away.
        EditHomeLink(
            onEditHome,
            Modifier
                .align(Alignment.End)
                .padding(top = LibraryDimens.laceScallop, end = LibraryDimens.screenPadding, bottom = LibraryDimens.spaceSm),
        )
        Column(
            Modifier.padding(horizontal = LibraryDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.sectionGap),
        ) {
            // While the library loads, show the loader (only if it is slow) rather than empty blocks.
            if (state.isLoading) {
                ShelfLoader(Modifier.padding(top = LibraryDimens.sectionGap))
            } else {
                state.blocks.forEach { block ->
                    HomeBlockContent(block, state, onOpenBook, onSearch, onSeeShelves, onOpenProgress)
                }
            }
            Spacer(Modifier.height(LibraryDimens.spaceLg))
        }
    }
}

/** One home block. A block with nothing to show yet (no finished books, no notes) draws nothing. */
@Composable
private fun HomeBlockContent(
    block: HomeBlock,
    state: HomeUiState,
    onOpenBook: (String) -> Unit,
    onSearch: () -> Unit,
    onSeeShelves: () -> Unit,
    onOpenProgress: () -> Unit,
) {
    when (block) {
        HomeBlock.CURRENTLY_READING -> {
            val current = state.currentlyReading
            if (current != null) LoanCard(book = current, onClick = { onOpenBook(current.id) }) else EmptyLoanCard(onSearch = onSearch)
        }
        HomeBlock.STREAK -> StreakCard(state.streak, state.today, onClick = onOpenProgress)
        HomeBlock.LICENCES -> if (state.licences.isNotEmpty()) LicencesCard(state.licences, onClick = onOpenProgress)
        HomeBlock.RECENTLY_FINISHED -> if (state.recentlyFinished.isNotEmpty()) {
            RecentlyFinishedShelf(books = state.recentlyFinished, onOpenBook = onOpenBook, onSeeAll = onSeeShelves)
        }
        HomeBlock.QUOTE_OF_THE_DAY -> state.quote?.let { QuoteCard(it) }
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
                // Empty while loading, so the row keeps its height and nothing jumps.
                text = if (state.isLoading) "" else libraryStats(state),
                style = type.chip,
                color = colors.inkMuted,
                modifier = Modifier.weight(1f),
            )
            LibraryChip(text = stringResource(R.string.calendar_title), onClick = onOpenCalendar)
        }
    }
}

/** "Edit home", under the lace on the right, so the header keeps the design's height. */
@Composable
private fun EditHomeLink(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.home_edit),
        style = LibraryTheme.typography.chip,
        color = LibraryTheme.colors.primary,
        modifier = modifier
            .heightIn(min = LibraryDimens.touchTarget)
            .clip(LibraryShapes.pill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = LibraryDimens.spaceSm, vertical = LibraryDimens.spaceMd),
    )
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
private fun EmptyLoanCard(onSearch: () -> Unit) = NoticeCard(
    title = stringResource(R.string.home_no_book_title),
    subtitle = stringResource(R.string.home_no_book_subtitle),
    actionLabel = stringResource(R.string.home_search_book),
    onAction = onSearch,
)

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

/** Days read in a row, this week's days and, until today counts, how little it takes. Opens the progress screen. */
@Composable
private fun StreakCard(streak: ReadingStreak, today: LocalDate, onClick: () -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    PaperCard(
        Modifier
            .fillMaxWidth()
            .clip(LibraryShapes.card)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.cd_open_progress), onClick = onClick),
    ) {
        Column(Modifier.padding(horizontal = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceMd)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.streak_label), style = type.label, color = colors.primary)
                Text(
                    pluralStringResource(R.plurals.streak_days, streak.current, streak.current),
                    style = type.handSmall,
                    color = colors.accentDeep,
                )
            }
            Spacer(Modifier.height(LibraryDimens.spaceSm))
            StreakWeek(today = today, daysRead = streak.daysRead)
            if (today.toEpochDay() !in streak.daysRead) {
                Spacer(Modifier.height(LibraryDimens.spaceSm))
                Text(
                    pluralStringResource(R.plurals.streak_today_hint, ReadingStreak.MIN_PAGES_PER_DAY, ReadingStreak.MIN_PAGES_PER_DAY),
                    style = type.handSmall,
                    color = colors.inkMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

/** The licence rosettes, as on the progress screen, which it opens. */
@Composable
private fun LicencesCard(licences: List<ShelfLicence>, onClick: () -> Unit) {
    PaperCard(
        Modifier
            .fillMaxWidth()
            .clip(LibraryShapes.card)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.cd_open_progress), onClick = onClick),
    ) {
        Box(Modifier.padding(horizontal = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceMd)) {
            Licences(licences)
        }
    }
}

/** Today's line from her notes, handwritten, with the book it came from. */
@Composable
private fun QuoteCard(quote: DailyQuote) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Box {
        PaperCard(Modifier.fillMaxWidth(), color = colors.primaryLight) {
            Column(Modifier.padding(horizontal = LibraryDimens.spaceLg, vertical = LibraryDimens.spaceMd)) {
                Text(stringResource(R.string.home_quote_label), style = type.label, color = colors.primary)
                Text(
                    stringResource(R.string.share_quote, quote.text),
                    style = type.emphasis,
                    color = colors.ink,
                    maxLines = QUOTE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = LibraryDimens.spaceSm),
                )
                quote.bookTitle?.let {
                    Text(it, style = type.handSmall, color = colors.accentDeep, modifier = Modifier.align(Alignment.End).padding(top = LibraryDimens.spaceXs))
                }
            }
        }
        WashiTape(Modifier.align(Alignment.TopEnd).offset(x = -LibraryDimens.sectionGap, y = -LibraryDimens.spaceSm))
    }
}

private const val QUOTE_MAX_LINES = 5

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
    streak = LocalDate.of(2026, 11, 20).toEpochDay().let { today -> ReadingStreak(current = 3, record = 5, daysRead = setOf(today - 2, today - 1, today)) },
    licences = listOf(ShelfLicence(ShelfTheme.ROMANTASY, 5), ShelfLicence(ShelfTheme.WITCHY, 1), ShelfLicence(ShelfTheme.COWBOY, 0), ShelfLicence(ShelfTheme.ROMANCE, 3), ShelfLicence(ShelfTheme.THRILLER, 2)),
    quote = DailyQuote("Hay libros que se leen y libros que se viven.", "Fourth Wing"),
    blocks = HomeBlock.entries,
    today = LocalDate.of(2026, 11, 20),
    isLoading = false,
)

@Preview(name = "Home · English", locale = "en", widthDp = 390, heightDp = 844)
@Preview(name = "Home · Spanish", locale = "es", widthDp = 390, heightDp = 844)
@Composable
private fun HomePreview() = LibraryTheme {
    HomeContent(previewState, DayPeriod.Afternoon, onOpenBook = {}, onSearch = {}, onSeeShelves = {}, onOpenCalendar = {}, onOpenProgress = {}, onEditHome = {})
}

@Preview(name = "Home · empty", locale = "es", widthDp = 390, heightDp = 844)
@Composable
private fun HomeEmptyPreview() = LibraryTheme {
    HomeContent(HomeUiState(isLoading = false), DayPeriod.Morning, onOpenBook = {}, onSearch = {}, onSeeShelves = {}, onOpenCalendar = {}, onOpenProgress = {}, onEditHome = {})
}

// endregion
