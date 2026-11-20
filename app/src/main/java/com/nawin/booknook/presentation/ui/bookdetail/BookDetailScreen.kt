package com.nawin.booknook.presentation.ui.bookdetail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.ui.note.NoteSheet
import com.nawin.booknook.presentation.ui.note.PageDraft
import com.nawin.booknook.presentation.ui.note.labelRes
import com.nawin.booknook.domain.model.ShelfLicence
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.presentation.components.library.LibraryChip
import com.nawin.booknook.presentation.theme.nameRes
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.presentation.components.library.CatalogueCover
import com.nawin.booknook.presentation.components.library.ArchFrame
import com.nawin.booknook.presentation.components.library.Daisy
import com.nawin.booknook.presentation.components.library.DateStamp
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.RATING_MAX
import com.nawin.booknook.presentation.components.library.RatingInput
import com.nawin.booknook.presentation.components.library.ReadingProgress
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.SegmentedPills
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.components.library.SpineCode
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    navController: NavController,
    viewModel: BookDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val book = uiState.book
    val colors = LibraryTheme.colors

    if (book == null) {
        Box(
            Modifier.fillMaxSize().background(colors.background).statusBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            ShelfLoader()
        }
        return
    }

    if (uiState.showNoteSheet) {
        val reading = book.status == ReadingStatus.READING
        NoteSheet(
            bookTitle = book.title,
            page = if (reading) PageDraft(book.currentPage, book.pageCount) else null,
            onDismiss = { viewModel.showNoteSheet(false) },
            onSave = viewModel::saveNote,
            onFinish = if (reading) {
                { mood, content ->
                    viewModel.finishFromNote(mood, content)
                    navController.navigate(Screen.NewStamp.createRoute(book.id))
                }
            } else null,
        )
    }

    // Insets on the outer column: scrolled content is clipped below the status bar,
    // and imePadding keeps the review field above the keyboard.
    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            BookSummary(
                book = book,
                onBack = { navController.popBackStack() },
                onToggleFavourite = viewModel::toggleFavorite,
                onStatusChange = { status ->
                    val justFinished = status == ReadingStatus.FINISHED && book.status != ReadingStatus.FINISHED
                    viewModel.updateStatus(status)
                    // Finishing earns a stamp: show it straight away.
                    if (justFinished) navController.navigate(Screen.NewStamp.createRoute(book.id))
                },
                onChangePage = viewModel::changePage,
                onEditPage = { viewModel.showNoteSheet(true) },
            )

            Spacer(Modifier.height(LibraryDimens.spaceMd))
            ReadingRecord(
                book = book,
                notes = uiState.notes,
                onRatingChange = viewModel::updateRating,
                onStartDateChange = viewModel::updateStartDate,
                onFinishDateChange = viewModel::updateFinishDate,
                onSaveReview = viewModel::updateReview,
                onShelfChange = viewModel::updateShelf,
            )
            Spacer(Modifier.height(LibraryDimens.sectionGap))
        }

        BookActions(
            onAddNote = { viewModel.showNoteSheet(true) },
            onShare = { navController.navigate(Screen.Share.createRoute(book.id)) },
        )
    }
}

/**
 * Top of the book page: the cover in an arch, title and author, the reading
 * status, the progress card while reading and the catalogue card.
 */
@Composable
private fun BookSummary(
    book: Book,
    onBack: () -> Unit,
    onToggleFavourite: () -> Unit,
    onStatusChange: (ReadingStatus) -> Unit,
    onChangePage: (Int) -> Unit,
    onEditPage: () -> Unit,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Column {
        CoverHeader(book, onBack, onToggleFavourite)
        Column(
            Modifier.padding(horizontal = LibraryDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(book.title, style = type.heading, color = colors.ink, textAlign = TextAlign.Center)
                Text(
                    book.author.ifBlank { stringResource(R.string.common_unknown_author) },
                    style = type.body,
                    color = colors.inkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = LibraryDimens.spaceXs),
                )
            }
            StatusSelector(book, onStatusChange)
            if (book.status == ReadingStatus.READING) {
                ProgressCard(book, onChangePage, onEditPage)
            }
            CatalogueCard(book)
        }
    }
}

/** Back and favourite buttons over the arch with the cover, a sprig and a daisy. */
@Composable
private fun CoverHeader(book: Book, onBack: () -> Unit, onToggleFavourite: () -> Unit) {
    val colors = LibraryTheme.colors
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier.align(Alignment.TopCenter).padding(top = LibraryDimens.spaceLg),
            contentAlignment = Alignment.TopCenter,
        ) {
            ArchFrame(Modifier.size(LibraryDimens.detailArchWidth, LibraryDimens.detailArchHeight))
            Sprig(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = -LibraryDimens.sprigSize / 2, y = -LibraryDimens.spaceLg),
            )
            Daisy(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = LibraryDimens.daisySize / 2, y = LibraryDimens.sectionGap),
            )
            Box(Modifier.padding(top = LibraryDimens.detailCoverInset)) {
                CatalogueCover(book, width = LibraryDimens.coverDetailWidth, height = LibraryDimens.coverDetailHeight)
                val finishDate = book.finishDate
                if (book.status == ReadingStatus.FINISHED && finishDate != null) {
                    DateStamp(
                        label = stringResource(R.string.stamp_read),
                        epochMillis = finishDate,
                        rotation = STAMP_ROTATION,
                        background = colors.background,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = LibraryDimens.stampSize - LibraryDimens.spaceXs, y = -LibraryDimens.spaceXs),
                    )
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = LibraryDimens.screenPadding - LibraryDimens.spaceXs, vertical = LibraryDimens.spaceXs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), onBack)
            RoundIconButton(
                icon = if (book.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(R.string.detail_favourite),
                onClick = onToggleFavourite,
                tint = colors.primary,
            )
        }
    }
}

/**
 * The three everyday statuses as a segmented pill. "Put it down" (DNF) is a
 * quiet link below, offered only while reading: four segments do not fit a
 * narrow phone, and abandoning only makes sense for a book in progress.
 */
@Composable
private fun StatusSelector(book: Book, onStatusChange: (ReadingStatus) -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        SegmentedPills(
            options = SELECTABLE_STATUSES.map { stringResource(it.labelRes) },
            selectedIndex = SELECTABLE_STATUSES.indexOf(book.status).takeIf { it >= 0 },
            onSelect = { onStatusChange(SELECTABLE_STATUSES[it]) },
        )
        if (book.status == ReadingStatus.DNF) {
            Text(
                text = if (book.currentPage > 0) {
                    stringResource(R.string.detail_put_down_at, book.currentPage)
                } else {
                    stringResource(R.string.status_dnf)
                },
                style = type.handSmall,
                color = colors.stamp,
                modifier = Modifier.padding(top = LibraryDimens.spaceSm),
            )
        } else if (book.status == ReadingStatus.READING) {
            Text(
                text = stringResource(R.string.detail_put_down),
                style = type.handSmall,
                color = colors.inkMuted,
                modifier = Modifier
                    .clip(LibraryShapes.pill)
                    .clickable(role = Role.Button) { onStatusChange(ReadingStatus.DNF) }
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = LibraryDimens.spaceSm),
            )
        }
    }
}

/** Current page with one-page steps; tapping the number opens the exact-page dialog. */
@Composable
private fun ProgressCard(book: Book, onChangePage: (Int) -> Unit, onEditPage: () -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val pageCount = book.pageCount
    PaperCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceMd)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.detail_your_progress), style = type.label, color = colors.primary)
                book.startDate?.let { started ->
                    Text(
                        stringResource(R.string.detail_started_on, formatDate(started)),
                        style = type.handSmall,
                        color = colors.accentDeep,
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = LibraryDimens.spaceXs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundIconButton(Icons.Filled.Remove, stringResource(R.string.cd_page_back), { onChangePage(-1) })
                Row(
                    Modifier
                        .clip(LibraryShapes.label)
                        .clickable(onClickLabel = stringResource(R.string.detail_update_progress), role = Role.Button, onClick = onEditPage)
                        .padding(horizontal = LibraryDimens.spaceSm, vertical = LibraryDimens.spaceXs),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(book.currentPage.toString(), style = type.heading, color = colors.ink)
                    if (pageCount != null) {
                        Text(
                            " " + pluralStringResource(R.plurals.detail_of_pages, pageCount, pageCount),
                            style = type.chip,
                            color = colors.inkMuted,
                            modifier = Modifier.padding(bottom = LibraryDimens.spaceXs),
                        )
                    }
                }
                RoundIconButton(
                    Icons.Filled.Add,
                    stringResource(R.string.cd_page_forward),
                    { onChangePage(1) },
                    fill = colors.highlight,
                )
            }
            if (pageCount != null && pageCount > 0) {
                ReadingProgress(book.currentPage.toFloat() / pageCount)
            }
        }
    }
}

/**
 * The book's library card: its spine code and the facts we have about the
 * edition. Rows without data are left out rather than shown empty.
 */
@Composable
private fun CatalogueCard(book: Book) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val spine = remember(book.author, book.title) { SpineCode.from(book.author, book.title) }
    val rows = buildList {
        book.publishYear?.let { add(stringResource(R.string.detail_catalogue_year) to it.toString()) }
        book.pageCount?.let { add(stringResource(R.string.detail_catalogue_pages) to it.toString()) }
    }
    PaperCard(Modifier.fillMaxWidth(), cornerRadius = LibraryDimens.radiusRow) {
        Column(
            Modifier.padding(
                start = LibraryDimens.spaceLg - LibraryDimens.spaceXxs,
                end = LibraryDimens.spaceLg - LibraryDimens.spaceXxs,
                top = LibraryDimens.spaceMd,
                bottom = LibraryDimens.spaceXl,
            ),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.detail_catalogue_card), style = type.label, color = colors.primary)
                if (spine.author.isNotEmpty()) {
                    Text(
                        "${spine.author} ${spine.title}",
                        style = type.label,
                        color = colors.ink,
                        modifier = Modifier
                            .background(colors.primaryLight, LibraryShapes.labelInner)
                            .padding(horizontal = LibraryDimens.spaceXs + LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceXxs),
                    )
                }
            }
            rows.forEach { (key, value) ->
                Row(Modifier.fillMaxWidth().padding(top = LibraryDimens.spaceXs)) {
                    Text(key, style = type.label, color = colors.inkMuted, modifier = Modifier.width(LibraryDimens.catalogueKeyWidth))
                    Text(value, style = type.chip, color = colors.ink)
                }
                HorizontalDivider(
                    Modifier.padding(top = LibraryDimens.spaceXs),
                    thickness = LibraryDimens.strokeHairline,
                    color = colors.outline,
                )
            }
        }
        // The punched hole a catalogue card hangs from in the drawer.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = LibraryDimens.spaceXs)
                .size(LibraryDimens.punchHole)
                .background(colors.background, LibraryShapes.pill)
                .border(LibraryDimens.stroke, colors.outline, LibraryShapes.pill),
        )
    }
}

/** "+ Note" and "Share progress", pinned above the system navigation bar. */
@Composable
private fun BookActions(onAddNote: () -> Unit, onShare: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(LibraryTheme.colors.background)
            .navigationBarsPadding()
            .padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd - LibraryDimens.spaceXxs),
    ) {
        PillButton(stringResource(R.string.detail_add_note_short), onClick = onAddNote, primary = false)
        PillButton(stringResource(R.string.detail_share), onClick = onShare, modifier = Modifier.weight(1f))
    }
}

/** A date in the locale's medium style, e.g. "2 nov 2026" or "Nov 2, 2026". */
@Composable
private fun formatDate(epochMillis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(epochMillis, locale) {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    }
}

/** Statuses offered in the segmented pill; DNF has its own link. */
private val SELECTABLE_STATUSES = listOf(ReadingStatus.WANT_TO_READ, ReadingStatus.READING, ReadingStatus.FINISHED)
private const val STAMP_ROTATION = 12f

private val ReadingStatus.labelRes: Int
    get() = when (this) {
        ReadingStatus.READING -> R.string.status_reading
        ReadingStatus.FINISHED -> R.string.status_finished
        ReadingStatus.WANT_TO_READ -> R.string.status_want_to_read
        ReadingStatus.DNF -> R.string.status_dnf
    }

/**
 * The reader's own record of the book, below the summary: shelf, rating,
 * reading dates, review and notes, each on its own paper card.
 */
@Composable
private fun ReadingRecord(
    book: Book,
    notes: List<BookNote>,
    onRatingChange: (Float) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onFinishDateChange: (Long) -> Unit,
    onSaveReview: (String) -> Unit,
    onShelfChange: (ShelfTheme?) -> Unit,
) {
    Column(
        Modifier.padding(horizontal = LibraryDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
    ) {
        ShelfCard(book.shelf, onShelfChange)
        RatingCard(book.rating, onRatingChange)
        DatesCard(book.startDate, book.finishDate, onStartDateChange, onFinishDateChange)
        ReviewCard(book.review.orEmpty(), onSaveReview)
        NotesCard(notes)
    }
}

/** A paper card with a small uppercase [label] over its [content]. */
@Composable
private fun RecordCard(
    label: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LibraryTheme.colors
    PaperCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceMd)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = LibraryTheme.typography.label, color = colors.primary)
                trailing()
            }
            content()
        }
    }
}

/** Which genre shelf the book is filed under; tapping the chosen one again clears it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShelfCard(shelf: ShelfTheme?, onShelfChange: (ShelfTheme?) -> Unit) {
    RecordCard(
        label = stringResource(R.string.detail_shelf),
        trailing = {
            Text(
                pluralStringResource(R.plurals.detail_shelf_hint, ShelfLicence.BOOKS_PER_LICENCE, ShelfLicence.BOOKS_PER_LICENCE),
                style = LibraryTheme.typography.handSmall,
                color = LibraryTheme.colors.accentDeep,
            )
        },
    ) {
        FlowRow(
            Modifier.padding(top = LibraryDimens.spaceSm),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
        ) {
            ShelfLicence.genreShelves.forEach { option ->
                LibraryChip(
                    text = stringResource(option.nameRes),
                    selected = option == shelf,
                    onClick = { onShelfChange(if (option == shelf) null else option) },
                )
            }
        }
    }
}

@Composable
private fun RatingCard(rating: Float, onRatingChange: (Float) -> Unit) {
    RecordCard(stringResource(R.string.detail_rating)) {
        RatingInput(
            rating = rating,
            onRatingChange = onRatingChange,
            // The noun agrees with the total ("1 of 5 stars"), so the quantity is RATING_MAX.
            starDescription = { stars -> pluralStringResource(R.plurals.cd_rating, RATING_MAX, stars, RATING_MAX) },
            modifier = Modifier.offset(x = -LibraryDimens.spaceSm),
        )
    }
}

/**
 * Start and finish dates side by side; each opens a date picker. The picker
 * works in UTC midnights, so dates are converted through [LocalDate] to keep
 * the same calendar day in any time zone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatesCard(
    startDate: Long?,
    finishDate: Long?,
    onStartDateChange: (Long) -> Unit,
    onFinishDateChange: (Long) -> Unit,
) {
    var editing by remember { mutableStateOf<DateField?>(null) }

    editing?.let { field ->
        val initial = if (field == DateField.START) startDate else finishDate
        val state = rememberDatePickerState(initialSelectedDateMillis = (initial ?: System.currentTimeMillis()).toPickerMillis())
        DatePickerDialog(
            onDismissRequest = { editing = null },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { picked ->
                        val local = picked.fromPickerMillis()
                        if (field == DateField.START) onStartDateChange(local) else onFinishDateChange(local)
                    }
                    editing = null
                }) { Text(stringResource(R.string.detail_save)) }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text(stringResource(R.string.common_cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    RecordCard(stringResource(R.string.detail_dates)) {
        Row(
            // Same height for both, whether a date or the taller handwritten hint is shown.
            Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(top = LibraryDimens.spaceSm),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
        ) {
            DateField(stringResource(R.string.detail_start_date), startDate, { editing = DateField.START }, Modifier.weight(1f).fillMaxHeight())
            DateField(stringResource(R.string.detail_finish_date), finishDate, { editing = DateField.FINISH }, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

private enum class DateField { START, FINISH }

@Composable
private fun DateField(label: String, date: Long?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Column(
        modifier
            .clip(LibraryShapes.label)
            .background(colors.background)
            .border(LibraryDimens.strokeHairline, colors.outline, LibraryShapes.label)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = LibraryDimens.spaceMd, vertical = LibraryDimens.spaceSm),
        verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXxs),
    ) {
        Text(label, style = type.label, color = colors.inkMuted)
        if (date != null) {
            Text(formatDate(date), style = type.chip, color = colors.ink)
        } else {
            Text(stringResource(R.string.detail_date_not_set), style = type.handSmall, color = colors.inkMuted)
        }
    }
}

/** Free-text review; "Save" appears only when the text differs from the saved one. */
@Composable
private fun ReviewCard(savedReview: String, onSave: (String) -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    var text by remember(savedReview) { mutableStateOf(savedReview) }
    RecordCard(stringResource(R.string.detail_my_review)) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = type.body.copy(color = colors.ink),
            cursorBrush = SolidColor(colors.primary),
            minLines = REVIEW_MIN_LINES,
            modifier = Modifier.fillMaxWidth().padding(top = LibraryDimens.spaceSm),
            decorationBox = { field ->
                Box {
                    if (text.isEmpty()) {
                        Text(stringResource(R.string.detail_write_review), style = type.body, color = colors.inkMuted)
                    }
                    field()
                }
            },
        )
        if (text.trim() != savedReview.trim()) {
            PillButton(
                stringResource(R.string.detail_save),
                onClick = { onSave(text.trim()) },
                modifier = Modifier.align(Alignment.End).padding(top = LibraryDimens.spaceSm),
            )
        }
    }
}

/** The book's notes, newest first, or a short handwritten hint when there are none. */
@Composable
private fun NotesCard(notes: List<BookNote>) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    RecordCard(
        label = stringResource(R.string.detail_notes_label),
        trailing = {
            if (notes.isNotEmpty()) Text(notes.size.toString(), style = type.label, color = colors.inkMuted)
        },
    ) {
        if (notes.isEmpty()) {
            Text(
                stringResource(R.string.detail_notes_empty),
                style = type.handSmall,
                color = colors.inkMuted,
                modifier = Modifier.padding(top = LibraryDimens.spaceSm),
            )
        }
        notes.sortedByDescending { it.createdAt }.forEachIndexed { index, note ->
            if (index > 0) {
                HorizontalDivider(thickness = LibraryDimens.strokeHairline, color = colors.outline)
            }
            Column(
                Modifier.fillMaxWidth().padding(vertical = LibraryDimens.spaceSm),
                verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXxs),
            ) {
                val where = listOfNotNull(
                    note.chapter,
                    note.page?.let { stringResource(R.string.detail_note_page, it) },
                ).joinToString(NOTE_META_SEPARATOR)
                if (where.isNotEmpty()) Text(where, style = type.label, color = colors.primary)
                note.mood?.let { LibraryChip(stringResource(it.labelRes)) }
                if (note.content.isNotEmpty()) Text(note.content, style = type.body, color = colors.ink)
                Text(formatDate(note.createdAt), style = type.handSmall, color = colors.inkMuted)
            }
        }
    }
}

/** Local midnight of the same calendar day, as the date picker expects it: UTC midnight. */
private fun Long.toPickerMillis(): Long =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/** The picker's UTC midnight back to local midnight of the same calendar day. */
private fun Long.fromPickerMillis(): Long =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private const val REVIEW_MIN_LINES = 3
private const val NOTE_META_SEPARATOR = " · "

// region Previews

private fun previewMillis(month: Int, day: Int) =
    LocalDate.of(2026, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private val previewReading = Book(
    "1", "Throne of Glass", "Sarah J. Maas", null, null, 2012, 404, null, ReadingStatus.READING,
    currentPage = 212, startDate = previewMillis(11, 2), isFavorite = true,
)

@Preview(name = "Book · reading", locale = "es", widthDp = 360, heightDp = 900, showBackground = true)
@Composable
private fun BookSummaryReadingPreview() = LibraryTheme {
    Box(Modifier.background(LibraryTheme.colors.background)) {
        BookSummary(previewReading, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Book · finished", locale = "en", widthDp = 360, heightDp = 700, showBackground = true)
@Composable
private fun BookSummaryFinishedPreview() = LibraryTheme {
    Box(Modifier.background(LibraryTheme.colors.background)) {
        BookSummary(previewReading.copy(status = ReadingStatus.FINISHED, finishDate = previewMillis(11, 15)), {}, {}, {}, {}, {})
    }
}

// endregion
