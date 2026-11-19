package com.nawin.booknook.presentation.ui.search

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.presentation.components.library.CatalogueCover
import com.nawin.booknook.presentation.components.library.DateStamp
import com.nawin.booknook.presentation.components.library.InkStamp
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.NoticeCard
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.components.library.drawFlatShadow
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import org.koin.compose.viewmodel.koinViewModel

/** Search tab: Open Library results, with the books already on the user's shelves marked. */
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    SearchContent(
        state = uiState,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onAdd = viewModel::addToLibrary,
        onOpenBook = { bookId -> navController.navigate(Screen.BookDetail.createRoute(bookId)) },
    )
}

/** Stateless search layout, driven only by [state] and [query] so it can be previewed and tested. */
@Composable
fun SearchContent(
    state: SearchUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onAdd: (Book) -> Unit,
    onOpenBook: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val gutter = Modifier.padding(horizontal = LibraryDimens.screenPadding)

    LazyColumn(
        modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(bottom = LibraryDimens.spaceXl),
    ) {
        item(key = KEY_HEADER) {
            Box {
                LaceHeader(height = LibraryDimens.headerHeightCompact + statusBarTop)
                SearchHeader(
                    Modifier.padding(
                        start = LibraryDimens.screenPadding,
                        end = LibraryDimens.screenPadding,
                        top = statusBarTop + LibraryDimens.spaceXl,
                    ),
                )
            }
        }
        item(key = KEY_FIELD) {
            SearchField(query, onQueryChange, gutter.padding(top = LibraryDimens.spaceLg))
        }

        val results = state.books
        when {
            state.isLoading -> item(key = KEY_STATUS) { Loading() }
            state.error != null -> item(key = KEY_STATUS) {
                Notice(R.string.search_error_title, R.string.search_error_subtitle, gutter)
            }
            results.isEmpty() && query.isBlank() -> item(key = KEY_STATUS) {
                Notice(R.string.search_empty_title, R.string.search_empty_subtitle, gutter)
            }
            results.isEmpty() -> item(key = KEY_STATUS) {
                Notice(R.string.search_no_results_title, R.string.search_no_results_subtitle, gutter)
            }
            else -> {
                if (results.first().id in state.library) {
                    item(key = KEY_SHELF_FIRST) { ShelfFirstNote(gutter) }
                }
                items(results, key = { it.id }) { book ->
                    val shelved = state.library[book.id]
                    ResultRow(
                        book = book,
                        shelved = shelved,
                        onAdd = { onAdd(book) },
                        onOpen = { onOpenBook(book.id) },
                        modifier = gutter.padding(vertical = LibraryDimens.spaceXs + LibraryDimens.spaceXxs),
                    )
                }
            }
        }
    }
}

private const val KEY_HEADER = "header"
private const val KEY_FIELD = "field"
private const val KEY_STATUS = "status"
private const val KEY_SHELF_FIRST = "shelf_first"

@Composable
private fun SearchHeader(modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Column(modifier.fillMaxWidth()) {
        Text(stringResource(R.string.search_scope), style = type.hand, color = colors.stamp)
        Text(stringResource(R.string.search_heading), style = type.title, color = colors.ink)
    }
}

/** Pill-shaped text field with a search icon and a round clear button. */
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val keyboard = LocalSoftwareKeyboardController.current
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = type.button.copy(color = colors.ink),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // Results already update while typing; the key only closes the keyboard.
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerField ->
            Row(
                Modifier
                    .heightIn(min = LibraryDimens.buttonMinHeight)
                    .drawBehind { drawFlatShadow(colors.shadow, CornerRadius(size.height / 2), LibraryDimens.shadowOffsetSmall) }
                    .background(colors.paper, LibraryShapes.pill)
                    .border(LibraryDimens.stroke, colors.outline, LibraryShapes.pill)
                    .padding(start = LibraryDimens.spaceLg, end = LibraryDimens.spaceXs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm + LibraryDimens.spaceXxs),
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = colors.ink, modifier = Modifier.size(LibraryDimens.navIcon))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            stringResource(R.string.search_placeholder),
                            style = type.button,
                            color = colors.inkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerField()
                }
                if (query.isNotEmpty()) ClearButton { onQueryChange("") }
            }
        },
    )
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    val colors = LibraryTheme.colors
    val description = stringResource(R.string.search_clear)
    Box(
        Modifier
            .minimumInteractiveComponentSize()
            .clip(LibraryShapes.pill)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(LibraryDimens.clearButton).background(colors.primaryLight, LibraryShapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Close, contentDescription = null, tint = colors.ink, modifier = Modifier.size(LibraryDimens.badgeIcon))
        }
    }
}

@Composable
private fun Loading() = ShelfLoader(Modifier.padding(vertical = LibraryDimens.sectionGap))

@Composable
private fun Notice(@StringRes titleRes: Int, @StringRes subtitleRes: Int, modifier: Modifier) = NoticeCard(
    title = stringResource(titleRes),
    subtitle = stringResource(subtitleRes),
    modifier = modifier.padding(top = LibraryDimens.sectionGap),
)

/** Handwritten note shown when the books on the user's shelves are listed first. */
@Composable
private fun ShelfFirstNote(modifier: Modifier = Modifier) = Text(
    stringResource(R.string.search_your_shelf_first),
    style = LibraryTheme.typography.handSmall,
    color = LibraryTheme.colors.accentDeep,
    modifier = modifier
        .fillMaxWidth()
        .padding(top = LibraryDimens.spaceLg, bottom = LibraryDimens.spaceXs),
)

/**
 * One search result. [shelved] is the library copy of the book, if any: those
 * rows open the book and show a stamp instead of the add button.
 */
@Composable
private fun ResultRow(
    book: Book,
    shelved: Book?,
    onAdd: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val separator = stringResource(R.string.home_stats_separator)
    val byline = listOfNotNull(book.author, book.publishYear?.toString()).joinToString(separator)
    val clickable = if (shelved != null) Modifier.clickable(role = Role.Button, onClick = onOpen) else Modifier

    PaperCard(
        modifier.fillMaxWidth().clip(LibraryShapes.row).then(clickable),
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
                Text(book.title, style = type.titleSmall, color = colors.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(LibraryDimens.spaceXxs))
                Text(byline, style = type.body, color = colors.inkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            ResultAction(shelved, onAdd)
        }
    }
}

/** Right side of a result: a READ stamp, an IN YOUR SHELF stamp, or the add button. */
@Composable
private fun ResultAction(shelved: Book?, onAdd: () -> Unit) {
    val finishedOn = shelved?.finishDate?.takeIf { shelved.status == ReadingStatus.FINISHED }
    when {
        finishedOn != null -> DateStamp(
            label = stringResource(R.string.stamp_read),
            epochMillis = finishedOn,
            size = LibraryDimens.stampSizeSmall,
            rotation = RESULT_STAMP_ROTATION,
        )
        shelved != null -> InkStamp(
            top = stringResource(R.string.stamp_in_your),
            middle = stringResource(R.string.stamp_check),
            bottom = stringResource(R.string.stamp_shelf),
            size = LibraryDimens.stampSizeSmall,
        )
        else -> RoundIconButton(
            icon = Icons.Filled.Add,
            contentDescription = stringResource(R.string.cd_add_to_shelves),
            onClick = onAdd,
            fill = LibraryTheme.colors.highlight,
        )
    }
}

private const val RESULT_STAMP_ROTATION = 8f

// region Previews

private val previewResults = listOf(
    Book("1", "Throne of Glass", "Sarah J. Maas", null, null, 2012, 404, null),
    Book("2", "A Court of Thorns and Roses", "Sarah J. Maas", null, null, 2015, 419, null),
    Book("3", "Heir of Fire", "Sarah J. Maas", null, null, 2014, 562, null),
)

private val previewState = SearchUiState(
    books = previewResults,
    library = mapOf(
        "1" to previewResults[0].copy(status = ReadingStatus.READING),
        "2" to previewResults[1].copy(status = ReadingStatus.FINISHED, finishDate = 1_793_188_800_000),
    ),
)

@Preview(name = "Search · English", locale = "en", widthDp = 390, heightDp = 844)
@Preview(name = "Search · Spanish", locale = "es", widthDp = 390, heightDp = 844)
@Composable
private fun SearchPreview() = LibraryTheme {
    SearchContent(previewState, query = "maas", onQueryChange = {}, onAdd = {}, onOpenBook = {})
}

@Preview(name = "Search · empty", locale = "es", widthDp = 390, heightDp = 844)
@Composable
private fun SearchEmptyPreview() = LibraryTheme {
    SearchContent(SearchUiState(), query = "", onQueryChange = {}, onAdd = {}, onOpenBook = {})
}

// endregion
