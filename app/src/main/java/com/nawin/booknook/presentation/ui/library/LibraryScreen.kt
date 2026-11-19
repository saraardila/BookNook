package com.nawin.booknook.presentation.ui.library

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.presentation.components.library.CatalogueCover
import com.nawin.booknook.presentation.components.library.DateStamp
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.LibraryChip
import com.nawin.booknook.presentation.components.library.NoticeCard
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.RATING_MAX
import com.nawin.booknook.presentation.components.library.ReadingProgress
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.components.library.StarRating
import com.nawin.booknook.presentation.components.library.StatusTag
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.navigation.navigateToTopLevel
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

/** Shelves tab: every book in the library as a catalogue card, filtered by status. */
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    LibraryContent(
        state = uiState,
        selectedFilter = selectedFilter,
        onFilterSelected = viewModel::setFilter,
        onOpenBook = { bookId -> navController.navigate(Screen.BookDetail.createRoute(bookId)) },
        onSearch = { navController.navigateToTopLevel(Screen.Search.route) },
    )
}

/** Stateless shelves layout, driven only by [state] so it can be previewed and tested. */
@Composable
fun LibraryContent(
    state: LibraryUiState,
    selectedFilter: LibraryFilter,
    onFilterSelected: (LibraryFilter) -> Unit,
    onOpenBook: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LazyColumn(
        modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(bottom = LibraryDimens.spaceXl),
    ) {
        item(key = KEY_HEADER) {
            Box {
                LaceHeader(height = LibraryDimens.headerHeightCompact + statusBarTop, color = colors.accentLight)
                ShelvesHeader(
                    totalBooks = state.totalBooks,
                    modifier = Modifier.padding(
                        start = LibraryDimens.screenPadding,
                        end = LibraryDimens.screenPadding,
                        top = statusBarTop + LibraryDimens.spaceXl,
                    ),
                )
            }
        }
        item(key = KEY_FILTERS) {
            FilterRow(state.counts, selectedFilter, onFilterSelected)
        }
        item(key = KEY_SECTION) {
            SectionLine(selectedFilter, state.books.size)
        }
        if (state.isEmpty) {
            item(key = KEY_EMPTY) {
                EmptyShelf(onSearch, Modifier.padding(horizontal = LibraryDimens.screenPadding))
            }
        } else {
            items(state.books, key = { it.id }) { book ->
                CatalogueRow(
                    book = book,
                    onClick = { onOpenBook(book.id) },
                    modifier = Modifier.padding(
                        horizontal = LibraryDimens.screenPadding,
                        vertical = LibraryDimens.spaceXs + LibraryDimens.spaceXxs,
                    ),
                )
            }
        }
    }
}

private const val KEY_HEADER = "header"
private const val KEY_FILTERS = "filters"
private const val KEY_SECTION = "section"
private const val KEY_EMPTY = "empty"

@Composable
private fun ShelvesHeader(totalBooks: Int, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(
                pluralStringResource(R.plurals.shelves_books_catalogued, totalBooks, totalBooks),
                style = type.hand,
                color = colors.accentDeep,
            )
            Text(stringResource(R.string.shelves_title), style = type.title, color = colors.ink)
        }
        Sprig(stem = colors.accentDeep, leaf = colors.paper)
    }
}

@Composable
private fun FilterRow(
    counts: Map<LibraryFilter, Int>,
    selected: LibraryFilter,
    onSelected: (LibraryFilter) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = LibraryDimens.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
        modifier = Modifier.padding(top = LibraryDimens.spaceLg),
    ) {
        items(LibraryFilter.entries, key = { it.name }) { filter ->
            LibraryChip(
                text = stringResource(R.string.shelves_filter_count, stringResource(filter.labelRes), counts[filter] ?: 0),
                selected = filter == selected,
                onClick = { onSelected(filter) },
            )
        }
    }
}

/** Small caps line naming the current filter and how many books it shows. */
@Composable
private fun SectionLine(filter: LibraryFilter, count: Int) {
    val locale = LocalConfiguration.current.locales[0]
    val text = listOf(
        stringResource(filter.labelRes),
        pluralStringResource(R.plurals.shelves_section_count, count, count),
    ).joinToString(stringResource(R.string.home_stats_separator)).uppercase(locale)
    Text(
        text,
        style = LibraryTheme.typography.label,
        color = LibraryTheme.colors.inkMuted,
        modifier = Modifier.padding(
            start = LibraryDimens.screenPadding,
            end = LibraryDimens.screenPadding,
            top = LibraryDimens.spaceLg,
            bottom = LibraryDimens.spaceSm,
        ),
    )
}

/** One book as a catalogue card: cover with its spine label, title, author and status line. */
@Composable
private fun CatalogueRow(book: Book, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    PaperCard(
        modifier
            .fillMaxWidth()
            .clip(LibraryShapes.row)
            .clickable(role = Role.Button, onClick = onClick),
        cornerRadius = LibraryDimens.radiusRow,
    ) {
        Row(
            Modifier.padding(
                start = LibraryDimens.spaceSm + LibraryDimens.spaceXxs,
                top = LibraryDimens.spaceSm + LibraryDimens.spaceXxs,
                bottom = LibraryDimens.spaceSm + LibraryDimens.spaceXxs,
                end = LibraryDimens.spaceMd,
            ),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CatalogueCover(book)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        book.title,
                        style = type.titleSmall,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (book.isFavorite) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = stringResource(R.string.cd_favourite),
                            tint = colors.blush,
                            modifier = Modifier.size(LibraryDimens.badgeIcon),
                        )
                    }
                }
                Text(book.author, style = type.body, color = colors.inkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(LibraryDimens.spaceXs + LibraryDimens.spaceXxs))
                StatusLine(book)
            }
        }
    }
}

/** What each status shows under the author: progress, rating and finish stamp, or where it was left. */
@Composable
private fun StatusLine(book: Book) {
    val colors = LibraryTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
    ) {
        StatusTag(stringResource(book.status.labelRes).uppercase(locale), book.status.tagColor())
        when (book.status) {
            ReadingStatus.READING -> {
                val pages = book.pageCount
                if (pages != null && pages > 0) {
                    ReadingProgress(
                        fraction = book.currentPage.toFloat() / pages,
                        height = LibraryDimens.progressHeightSmall,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            ReadingStatus.FINISHED -> {
                if (book.rating > 0f) {
                    val stars = book.rating.roundToInt()
                    StarRating(book.rating, pluralStringResource(R.plurals.cd_rating, RATING_MAX, stars, RATING_MAX))
                }
                Spacer(Modifier.weight(1f))
                book.finishDate?.let { finished ->
                    DateStamp(
                        label = stringResource(R.string.stamp_read),
                        epochMillis = finished,
                        size = LibraryDimens.stampSizeSmall,
                        rotation = FINISH_STAMP_ROTATION,
                    )
                }
            }
            ReadingStatus.DNF -> if (book.currentPage > 0) {
                Text(
                    stringResource(R.string.shelves_dnf_at_page, book.currentPage),
                    style = LibraryTheme.typography.handSmall,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ReadingStatus.WANT_TO_READ -> Unit
        }
    }
}

private const val FINISH_STAMP_ROTATION = -10f

@Composable
private fun EmptyShelf(onSearch: () -> Unit, modifier: Modifier = Modifier) = NoticeCard(
    title = stringResource(R.string.library_empty_title),
    subtitle = stringResource(R.string.library_empty_subtitle),
    modifier = modifier,
    actionLabel = stringResource(R.string.home_search_book),
    onAction = onSearch,
)

@get:StringRes
private val LibraryFilter.labelRes: Int
    get() = when (this) {
        LibraryFilter.ALL -> R.string.filter_all
        LibraryFilter.READING -> R.string.filter_reading
        LibraryFilter.FINISHED -> R.string.filter_finished
        LibraryFilter.WANT_TO_READ -> R.string.filter_want_to_read
        LibraryFilter.DNF -> R.string.filter_dnf
        LibraryFilter.FAVORITES -> R.string.filter_favourites
    }

@get:StringRes
private val ReadingStatus.labelRes: Int
    get() = when (this) {
        ReadingStatus.READING -> R.string.status_reading
        ReadingStatus.FINISHED -> R.string.status_finished
        ReadingStatus.WANT_TO_READ -> R.string.status_want_to_read
        ReadingStatus.DNF -> R.string.status_dnf
    }

@Composable
private fun ReadingStatus.tagColor(): Color {
    val colors = LibraryTheme.colors
    return when (this) {
        ReadingStatus.READING -> colors.accentLight
        ReadingStatus.FINISHED -> colors.accent
        ReadingStatus.WANT_TO_READ -> colors.primaryLight
        ReadingStatus.DNF -> colors.blush
    }
}

// region Previews

private fun previewMillis(month: Int, day: Int) =
    LocalDate.of(2026, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private val previewBooks = listOf(
    Book("1", "Throne of Glass", "Sarah J. Maas", null, null, 2012, 404, null, ReadingStatus.READING, currentPage = 212),
    Book(
        "2", "A Court of Thorns and Roses", "Sarah J. Maas", null, null, 2015, 419, null, ReadingStatus.FINISHED,
        rating = 4f, finishDate = previewMillis(10, 28), isFavorite = true,
    ),
    Book("3", "Fourth Wing", "Rebecca Yarros", null, null, 2023, 498, null, ReadingStatus.READING, currentPage = 90),
    Book("4", "Heir of Fire", "Sarah J. Maas", null, null, 2014, 562, null, ReadingStatus.WANT_TO_READ),
    Book("5", "Six of Crows", "Leigh Bardugo", null, null, 2015, 465, null, ReadingStatus.DNF, currentPage = 140),
)

private val previewState = LibraryUiState(
    books = previewBooks,
    totalBooks = 27,
    counts = mapOf(
        LibraryFilter.ALL to 27,
        LibraryFilter.READING to 2,
        LibraryFilter.FINISHED to 9,
        LibraryFilter.WANT_TO_READ to 14,
        LibraryFilter.DNF to 2,
        LibraryFilter.FAVORITES to 5,
    ),
)

@Preview(name = "Shelves · English", locale = "en", widthDp = 390, heightDp = 844)
@Preview(name = "Shelves · Spanish", locale = "es", widthDp = 390, heightDp = 844)
@Composable
private fun ShelvesPreview() = LibraryTheme {
    LibraryContent(previewState, LibraryFilter.ALL, onFilterSelected = {}, onOpenBook = {}, onSearch = {})
}

@Preview(name = "Shelves · empty", locale = "es", widthDp = 390, heightDp = 844)
@Composable
private fun ShelvesEmptyPreview() = LibraryTheme {
    LibraryContent(LibraryUiState(isEmpty = true), LibraryFilter.ALL, onFilterSelected = {}, onOpenBook = {}, onSearch = {})
}

// endregion
