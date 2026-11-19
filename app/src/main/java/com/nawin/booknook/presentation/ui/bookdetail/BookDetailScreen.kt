package com.nawin.booknook.presentation.ui.bookdetail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.presentation.components.CozyCard
import com.nawin.booknook.presentation.components.library.CatalogueCover
import com.nawin.booknook.presentation.components.library.ArchFrame
import com.nawin.booknook.presentation.components.library.Daisy
import com.nawin.booknook.presentation.components.library.DateStamp
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.ReadingProgress
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.SegmentedPills
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.components.library.SpineCode
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    navController: NavController,
    viewModel: BookDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val book = uiState.book
    var showShareDialog by remember { mutableStateOf(false) }
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

    if (showShareDialog) {
        ShareCardDialog(book = book, onDismiss = { showShareDialog = false })
    }

    if (uiState.showProgressDialog) {
        ProgressUpdateDialog(
            currentPage = book.currentPage,
            totalPages = book.pageCount ?: 0,
            onConfirm = { page ->
                viewModel.updateProgress(page)
                viewModel.showProgressDialog(false)
            },
            onDismiss = { viewModel.showProgressDialog(false) }
        )
    }

    if (uiState.showNoteDialog) {
        AddNoteDialog(
            onConfirm = { content, chapter ->
                viewModel.addNote(content, chapter)
                viewModel.showNoteDialog(false)
            },
            onDismiss = { viewModel.showNoteDialog(false) }
        )
    }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            BookSummary(
                book = book,
                onBack = { navController.popBackStack() },
                onToggleFavourite = viewModel::toggleFavorite,
                onStatusChange = viewModel::updateStatus,
                onChangePage = viewModel::changePage,
                onEditPage = { viewModel.showProgressDialog(true) },
            )

            Spacer(modifier = Modifier.height(LibraryDimens.sectionGap))

            // Rating
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.detail_rating),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                StarRating(
                    rating = book.rating,
                    onRatingChange = { viewModel.updateRating(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dates
            DateSection(
                startDate = book.startDate,
                finishDate = book.finishDate,
                onStartDateChange = { viewModel.updateStartDate(it) },
                onFinishDateChange = { viewModel.updateFinishDate(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Review
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.detail_my_review),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                var reviewText by remember { mutableStateOf(book.review ?: "") }
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.detail_write_review),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { viewModel.updateReview(reviewText) },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.detail_save),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notes
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.detail_notes),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.notes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.detail_notes_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.notes.forEach { note -> NoteCard(note = note) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(LibraryDimens.sectionGap))
        }

        BookActions(
            onAddNote = { viewModel.showNoteDialog(true) },
            onShare = { showShareDialog = true },
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
    Box(Modifier.fillMaxWidth().statusBarsPadding()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSection(
    startDate: Long?,
    finishDate: Long?,
    onStartDateChange: (Long) -> Unit,
    onFinishDateChange: (Long) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showFinishPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onStartDateChange(it) }
                    showStartPicker = false
                }) { Text(stringResource(R.string.detail_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        ) { DatePicker(state = state) }
    }

    if (showFinishPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = finishDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showFinishPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onFinishDateChange(it) }
                    showFinishPicker = false
                }) { Text(stringResource(R.string.detail_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showFinishPicker = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        ) { DatePicker(state = state) }
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_dates),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateChip(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.detail_start_date),
                date = startDate?.let { dateFormat.format(Date(it)) },
                emoji = "📖",
                onClick = { showStartPicker = true }
            )
            DateChip(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.detail_finish_date),
                date = finishDate?.let { dateFormat.format(Date(it)) },
                emoji = "✅",
                onClick = { showFinishPicker = true }
            )
        }
    }
}

@Composable
fun DateChip(
    modifier: Modifier = Modifier,
    label: String,
    date: String?,
    emoji: String,
    onClick: () -> Unit
) {
    CozyCard(
        modifier = modifier,
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = date ?: stringResource(R.string.detail_date_not_set),
                style = MaterialTheme.typography.labelLarge,
                color = if (date != null) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun StarRating(rating: Float, onRatingChange: (Float) -> Unit, maxStars: Int = 5) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..maxStars) {
            IconButton(onClick = { onRatingChange(i.toFloat()) }, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun NoteCard(note: BookNote) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (note.chapter != null) {
                Text(text = note.chapter, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(text = note.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = dateFormat.format(Date(note.createdAt)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProgressUpdateDialog(currentPage: Int, totalPages: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var pageInput by remember { mutableStateOf(currentPage.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.detail_update_progress), style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.detail_current_page), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { pageInput = it.filter { c -> c.isDigit() } },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                if (totalPages > 0) {
                    val progress = (pageInput.toIntOrNull() ?: 0).toFloat() / totalPages
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pageInput.toIntOrNull() ?: currentPage) }) {
                Text(text = stringResource(R.string.detail_save), style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.common_cancel), style = MaterialTheme.typography.labelLarge) }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AddNoteDialog(onConfirm: (content: String, chapter: String?) -> Unit, onDismiss: () -> Unit) {
    var content by remember { mutableStateOf("") }
    var chapter by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.detail_notes), style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    placeholder = { Text(text = stringResource(R.string.detail_chapter), style = MaterialTheme.typography.bodyMedium) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text(text = stringResource(R.string.detail_add_note), style = MaterialTheme.typography.bodyMedium) },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(content, chapter) }) {
                Text(text = stringResource(R.string.detail_save), style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.common_cancel), style = MaterialTheme.typography.labelLarge) }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

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
