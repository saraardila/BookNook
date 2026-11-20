package com.nawin.booknook.presentation.ui.calendar

import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import com.nawin.booknook.domain.usecase.GetPagesPerDayUseCase
import com.nawin.booknook.util.FakeBookRepository
import com.nawin.booknook.util.FakeReadingLogRepository
import com.nawin.booknook.util.MainDispatcherRule
import com.nawin.booknook.util.book
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 20)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val library = FakeBookRepository()
    private val readingLog = FakeReadingLogRepository()
    private val viewModel by lazy {
        CalendarViewModel(GetMyLibraryUseCase(library), GetPagesPerDayUseCase(readingLog), clock)
    }

    private fun LocalDate.millis() = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    @Test
    fun `the month shows its starts and endings, and only the days that count for the streak`() = runTest(mainDispatcherRule.dispatcher) {
        library.library.value = listOf(
            book("fw").copy(status = ReadingStatus.READING, startDate = LocalDate.of(2026, 11, 2).millis()),
            book("circe").copy(
                status = ReadingStatus.FINISHED,
                startDate = LocalDate.of(2026, 10, 20).millis(),
                finishDate = LocalDate.of(2026, 11, 14).millis(),
            ),
            book("dnf").copy(status = ReadingStatus.DNF, finishDate = LocalDate.of(2026, 11, 9).millis()),
            // Back to reading: its old finish date is not shown, like in the journal.
            book("again").copy(status = ReadingStatus.READING, finishDate = LocalDate.of(2026, 11, 5).millis()),
        )
        readingLog.logPages(LocalDate.of(2026, 11, 3).toEpochDay(), "fw", 12)
        readingLog.logPages(LocalDate.of(2026, 11, 4).toEpochDay(), "fw", 2)
        readingLog.logPages(LocalDate.of(2026, 10, 30).toEpochDay(), "fw", 40)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(YearMonth.of(2026, 11), state.month)
        assertEquals(
            listOf("fw" to CalendarEventKind.STARTED, "dnf" to CalendarEventKind.PUT_DOWN, "circe" to CalendarEventKind.FINISHED),
            state.events.map { it.book.id to it.kind },
        )
        assertEquals(1, state.startedCount)
        assertEquals(1, state.finishedCount)
        assertEquals(setOf(3), state.readingDays)
    }

    @Test
    fun `she can go back in time but not past the current month`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canGoForward)

        viewModel.nextMonth()
        advanceUntilIdle()
        assertEquals(YearMonth.of(2026, 11), viewModel.uiState.value.month)

        viewModel.previousMonth()
        advanceUntilIdle()
        assertEquals(YearMonth.of(2026, 10), viewModel.uiState.value.month)
        assertTrue(viewModel.uiState.value.canGoForward)
    }
}
