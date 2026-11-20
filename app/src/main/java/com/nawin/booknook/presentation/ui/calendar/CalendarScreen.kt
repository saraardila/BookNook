package com.nawin.booknook.presentation.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.model.ReadingStreak
import com.nawin.booknook.presentation.components.BookCover
import com.nawin.booknook.presentation.components.library.CatalogueCover
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/** Reading calendar, opened from the home header. */
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarContent(
        state = state,
        onBack = { navController.popBackStack() },
        onPrevious = viewModel::previousMonth,
        onNext = viewModel::nextMonth,
        onOpenBook = { navController.navigate(Screen.BookDetail.createRoute(it)) },
    )
}

@Composable
private fun CalendarContent(
    state: CalendarUiState,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenBook: (String) -> Unit,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Box {
            LaceHeader(height = LibraryDimens.headerHeightCompact + statusBarTop)
            Column(Modifier.padding(start = LibraryDimens.screenPadding, end = LibraryDimens.screenPadding, top = statusBarTop + LibraryDimens.spaceLg)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd)) {
                    RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), onClick = onBack)
                    Text(stringResource(R.string.calendar_tagline), style = type.hand, color = colors.stamp)
                }
                Text(
                    stringResource(R.string.calendar_title),
                    style = type.heading,
                    color = colors.ink,
                    modifier = Modifier.padding(top = LibraryDimens.spaceSm).semantics { heading() },
                )
            }
        }

        Column(
            Modifier.padding(start = LibraryDimens.screenPadding, end = LibraryDimens.screenPadding, top = LibraryDimens.spaceXl),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceLg),
        ) {
            MonthSwitcher(state, onPrevious, onNext)
            MonthStats(state)
            MonthGrid(state, onOpenBook)
            if (state.isLoading) {
                ShelfLoader(Modifier.fillMaxWidth())
            } else {
                MonthBooks(state.events, onOpenBook)
            }
            Spacer(Modifier.height(LibraryDimens.sectionGap))
        }
    }
}

@Composable
private fun MonthSwitcher(state: CalendarUiState, onPrevious: () -> Unit, onNext: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val title = remember(state.month, locale) { monthTitle(state.month, locale) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RoundIconButton(Icons.Filled.ChevronLeft, stringResource(R.string.calendar_prev_month), onClick = onPrevious)
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = LibraryTheme.typography.titleSmall, color = LibraryTheme.colors.ink, modifier = Modifier.semantics { heading() })
            Text(state.month.year.toString(), style = LibraryTheme.typography.handSmall, color = LibraryTheme.colors.inkMuted)
        }
        // Faded and inert on the current month, so the row keeps its balance.
        RoundIconButton(
            Icons.Filled.ChevronRight,
            stringResource(R.string.calendar_next_month),
            onClick = { if (state.canGoForward) onNext() },
            modifier = Modifier.alpha(if (state.canGoForward) 1f else DISABLED_ALPHA),
        )
    }
}

@Composable
private fun MonthStats(state: CalendarUiState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd)) {
        StatCard(state.startedCount, stringResource(R.string.calendar_started), Modifier.weight(1f))
        StatCard(state.finishedCount, stringResource(R.string.calendar_finished), Modifier.weight(1f))
        StatCard(state.readingDays.size, stringResource(R.string.calendar_reading_days), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(value: Int, label: String, modifier: Modifier = Modifier) {
    PaperCard(modifier, cornerRadius = LibraryDimens.radiusRow) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = LibraryDimens.spaceMd, horizontal = LibraryDimens.spaceXs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value.toString(), style = LibraryTheme.typography.heading, color = LibraryTheme.colors.primary)
            Text(
                label,
                style = LibraryTheme.typography.label,
                color = LibraryTheme.colors.inkMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// region Grid

@Composable
private fun MonthGrid(state: CalendarUiState, onOpenBook: (String) -> Unit) {
    val colors = LibraryTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    val firstDay = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val weekDays = remember(firstDay) { (0 until DAYS_IN_WEEK).map { firstDay.plus(it.toLong()) } }
    // Empty cells before the 1st, counted from the locale's first day of the week.
    val leading = (state.month.atDay(1).dayOfWeek.value - firstDay.value + DAYS_IN_WEEK) % DAYS_IN_WEEK
    val days = state.month.lengthOfMonth()
    val rows = (leading + days + DAYS_IN_WEEK - 1) / DAYS_IN_WEEK
    val eventsByDay = state.eventsByDay

    PaperCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(LibraryDimens.spaceMd), verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs)) {
            Row(Modifier.fillMaxWidth().clearAndSetSemantics { }) {
                weekDays.forEach { day ->
                    Text(
                        day.getDisplayName(TextStyle.NARROW, locale),
                        style = LibraryTheme.typography.label,
                        color = colors.inkMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            for (row in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (column in 0 until DAYS_IN_WEEK) {
                        val day = row * DAYS_IN_WEEK + column - leading + 1
                        Box(Modifier.weight(1f).aspectRatio(DAY_CELL_RATIO).padding(LibraryDimens.spaceXxs)) {
                            if (day in 1..days) {
                                DayCell(
                                    date = state.month.atDay(day),
                                    events = eventsByDay[day].orEmpty(),
                                    read = day in state.readingDays,
                                    isToday = state.month.atDay(day) == state.today,
                                    onOpenBook = onOpenBook,
                                )
                            }
                        }
                    }
                }
            }
            Legend()
        }
    }
}

/**
 * A day: the cover of the book started or finished that day, a filled disc when
 * she read enough for the streak, an outline for today.
 */
@Composable
private fun DayCell(date: LocalDate, events: List<CalendarEvent>, read: Boolean, isToday: Boolean, onOpenBook: (String) -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val description = dayDescription(date, events, read, isToday)
    val book = events.firstOrNull()?.book
    val shape = LibraryShapes.label
    val base = Modifier
        .fillMaxSize()
        .clip(shape)
        .then(if (isToday) Modifier.border(LibraryDimens.stroke, colors.primary, shape) else Modifier)
        .then(
            if (book != null) Modifier.clickable(role = Role.Button, onClick = { onOpenBook(book.id) }) else Modifier,
        )
        .clearAndSetSemantics { contentDescription = description }

    Box(base, contentAlignment = Alignment.Center) {
        if (book != null) {
            BookCover(title = book.title, author = book.author, coverUrl = book.coverUrl, modifier = Modifier.fillMaxSize(), cornerRadius = LibraryDimens.radiusLabel)
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(LibraryDimens.spaceXxs)
                    .size(LibraryDimens.calendarDayBadge)
                    .background(colors.paper, CircleShape)
                    .border(LibraryDimens.strokeHairline, colors.outline, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(date.dayOfMonth.toString(), style = type.caption, color = colors.ink)
            }
            if (events.size > 1) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(LibraryDimens.spaceXxs)
                        .size(LibraryDimens.calendarDayBadge)
                        .background(colors.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.calendar_more, events.size - 1), style = type.caption, color = colors.onPrimary)
                }
            }
        } else {
            Box(
                Modifier
                    .size(LibraryDimens.calendarReadDisc)
                    .then(if (read) Modifier.background(colors.primaryLight, CircleShape) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    date.dayOfMonth.toString(),
                    style = type.chip,
                    color = if (read || isToday) colors.ink else colors.inkMuted,
                )
            }
        }
    }
}

@Composable
private fun Legend() {
    val colors = LibraryTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(top = LibraryDimens.spaceSm),
        horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(LibraryDimens.calendarLegendDot).background(colors.primaryLight, CircleShape).border(LibraryDimens.strokeHairline, colors.outline, CircleShape))
        Text(
            pluralStringResource(R.plurals.calendar_legend_read, ReadingStreak.MIN_PAGES_PER_DAY, ReadingStreak.MIN_PAGES_PER_DAY),
            style = LibraryTheme.typography.caption,
            color = colors.inkMuted,
        )
    }
}

@Composable
private fun dayDescription(date: LocalDate, events: List<CalendarEvent>, read: Boolean, isToday: Boolean): String {
    val locale = LocalConfiguration.current.locales[0]
    val parts = buildList {
        add(date.format(DateTimeFormatter.ofPattern(DAY_MONTH_PATTERN, locale)))
        if (isToday) add(stringResource(R.string.calendar_today))
        events.forEach { add(stringResource(it.kind.labelRes, it.book.title)) }
        if (read) add(stringResource(R.string.calendar_read_day))
    }
    return parts.joinToString(", ")
}

// endregion

// region Books of the month

@Composable
private fun MonthBooks(events: List<CalendarEvent>, onOpenBook: (String) -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Text(
        stringResource(R.string.calendar_this_month),
        style = type.label,
        color = colors.primary,
        modifier = Modifier.padding(top = LibraryDimens.spaceSm).semantics { heading() },
    )
    if (events.isEmpty()) {
        Text(stringResource(R.string.calendar_empty_month), style = type.hand, color = colors.inkMuted)
        return
    }
    events.forEach { event -> EventRow(event, onClick = { onOpenBook(event.book.id) }) }
}

@Composable
private fun EventRow(event: CalendarEvent, onClick: () -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val locale = LocalConfiguration.current.locales[0]
    val date = remember(event.date, locale) { event.date.format(DateTimeFormatter.ofPattern(DAY_MONTH_PATTERN, locale)) }
    PaperCard(Modifier.fillMaxWidth().clip(LibraryShapes.row).clickable(onClick = onClick), cornerRadius = LibraryDimens.radiusRow) {
        Row(
            Modifier.padding(LibraryDimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CatalogueCover(event.book)
            Column(Modifier.weight(1f)) {
                Text(event.book.title, style = type.titleSmall, color = colors.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(event.book.author, style = type.body, color = colors.inkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    stringResource(event.kind.stampRes, date),
                    style = type.handSmall,
                    color = if (event.kind == CalendarEventKind.STARTED) colors.accentDeep else colors.stamp,
                )
            }
        }
    }
}

private val CalendarEventKind.labelRes: Int
    get() = when (this) {
        CalendarEventKind.STARTED -> R.string.calendar_cd_started
        CalendarEventKind.FINISHED -> R.string.calendar_cd_finished
        CalendarEventKind.PUT_DOWN -> R.string.calendar_cd_put_down
    }

private val CalendarEventKind.stampRes: Int
    get() = when (this) {
        CalendarEventKind.STARTED -> R.string.calendar_stamp_started
        CalendarEventKind.FINISHED -> R.string.calendar_stamp_finished
        CalendarEventKind.PUT_DOWN -> R.string.calendar_stamp_put_down
    }

// endregion

/** Month name as it stands alone ("noviembre", "November"), capitalised for a title. */
private fun monthTitle(month: YearMonth, locale: Locale): String =
    month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }

private const val DAYS_IN_WEEK = 7
private const val DAY_CELL_RATIO = 0.8f
private const val DISABLED_ALPHA = 0.4f

/** Day and short month, e.g. "14 nov" / "Nov 14", in the locale's own order. */
private const val DAY_MONTH_PATTERN = "d MMM"

@Preview(name = "Calendar", locale = "es", widthDp = 360, heightDp = 1100, showBackground = true)
@Composable
private fun CalendarPreview() = LibraryTheme {
    fun sample(id: String, title: String, author: String, status: ReadingStatus) =
        Book(id, title, author, coverUrl = null, description = null, publishYear = null, pageCount = null, isbn = null, status = status)
    val fourthWing = sample("fw", "Fourth Wing", "Rebecca Yarros", ReadingStatus.READING)
    val circe = sample("c", "Circe", "Madeline Miller", ReadingStatus.FINISHED)
    CalendarContent(
        state = CalendarUiState(
            month = YearMonth.of(2026, 11),
            today = LocalDate.of(2026, 11, 20),
            events = listOf(
                CalendarEvent(fourthWing, LocalDate.of(2026, 11, 2), CalendarEventKind.STARTED),
                CalendarEvent(circe, LocalDate.of(2026, 11, 14), CalendarEventKind.FINISHED),
            ),
            readingDays = setOf(3, 4, 5, 9, 14, 18, 19),
            isLoading = false,
        ),
        onBack = {},
        onPrevious = {},
        onNext = {},
        onOpenBook = {},
    )
}
