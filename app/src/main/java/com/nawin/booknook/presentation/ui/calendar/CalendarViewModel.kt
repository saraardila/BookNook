package com.nawin.booknook.presentation.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.model.ReadingStreak
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import com.nawin.booknook.domain.usecase.GetPagesPerDayUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** What a day of the calendar records about a book. */
enum class CalendarEventKind { STARTED, FINISHED, PUT_DOWN }

data class CalendarEvent(val book: Book, val date: LocalDate, val kind: CalendarEventKind)

/**
 * @property events This month's starts and endings, oldest first.
 * @property readingDays Days of this month with at least [ReadingStreak.MIN_PAGES_PER_DAY] pages, the ones the streak counts.
 * @property canGoForward False on the current month: the future has nothing to show.
 */
data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    val events: List<CalendarEvent> = emptyList(),
    val readingDays: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
) {
    val eventsByDay: Map<Int, List<CalendarEvent>> get() = events.groupBy { it.date.dayOfMonth }
    val startedCount: Int get() = events.count { it.kind == CalendarEventKind.STARTED }
    val finishedCount: Int get() = events.count { it.kind == CalendarEventKind.FINISHED }
    val canGoForward: Boolean get() = month < YearMonth.from(today)
}

/**
 * Reading calendar: the books started and finished each month, and the days she
 * read enough for the streak. Opens on the current month.
 */
class CalendarViewModel(
    getMyLibrary: GetMyLibraryUseCase,
    getPagesPerDay: GetPagesPerDayUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val today = LocalDate.now(clock)
    private val month = MutableStateFlow(YearMonth.from(today))

    val uiState: StateFlow<CalendarUiState> =
        combine(getMyLibrary(), getPagesPerDay(), month) { books, pagesPerDay, month ->
            CalendarUiState(
                month = month,
                today = today,
                events = eventsIn(month, books, clock.zone),
                readingDays = readingDaysIn(month, pagesPerDay),
                isLoading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), CalendarUiState(month.value, today))

    fun previousMonth() = month.update { it.minusMonths(1) }

    fun nextMonth() = month.update { if (it < YearMonth.from(today)) it.plusMonths(1) else it }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L

        fun eventsIn(month: YearMonth, books: List<Book>, zone: ZoneId): List<CalendarEvent> {
            fun Long.toDate() = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
            val events = books.flatMap { book ->
                buildList {
                    book.startDate?.let { add(CalendarEvent(book, it.toDate(), CalendarEventKind.STARTED)) }
                    // A finish date only counts while the book is still finished or put down,
                    // the same rule as the journal and the reading card.
                    val ending = when (book.status) {
                        ReadingStatus.FINISHED -> CalendarEventKind.FINISHED
                        ReadingStatus.DNF -> CalendarEventKind.PUT_DOWN
                        else -> null
                    }
                    if (ending != null) book.finishDate?.let { add(CalendarEvent(book, it.toDate(), ending)) }
                }
            }
            return events.filter { YearMonth.from(it.date) == month }.sortedBy { it.date }
        }

        fun readingDaysIn(month: YearMonth, pagesPerDay: Map<Long, Int>): Set<Int> =
            pagesPerDay
                .filterValues { it >= ReadingStreak.MIN_PAGES_PER_DAY }
                .keys
                .map(LocalDate::ofEpochDay)
                .filter { YearMonth.from(it) == month }
                .mapTo(mutableSetOf()) { it.dayOfMonth }
    }
}
