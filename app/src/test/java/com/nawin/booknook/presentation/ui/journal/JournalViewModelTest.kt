package com.nawin.booknook.presentation.ui.journal

import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingMood
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.AddNoteUseCase
import com.nawin.booknook.domain.usecase.GetJournalUseCase
import com.nawin.booknook.util.FakeBookRepository
import com.nawin.booknook.util.MainDispatcherRule
import com.nawin.booknook.util.book
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookRepository()
    private val clock = Clock.fixed(LocalDate.of(2026, 11, 20).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    private val viewModel by lazy { JournalViewModel(GetJournalUseCase(repository), AddNoteUseCase(repository), clock) }

    private fun millis(month: Int, day: Int) = LocalDate.of(2026, month, day).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    @Test
    fun `entries are split by month and the newest month is shown first`() = runTest {
        repository.library.value = listOf(book("acotar").copy(status = ReadingStatus.FINISHED, finishDate = millis(10, 28)))
        repository.notes.value = listOf(BookNote(1, null, null, "Hay libros que se viven", null, null, millis(11, 17)))
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(YearMonth.of(2026, 11), YearMonth.of(2026, 10)), state.months)
        assertEquals(YearMonth.of(2026, 11), state.selectedMonth)
        assertEquals(listOf("note-1"), state.entries.map { it.key })

        viewModel.selectMonth(YearMonth.of(2026, 10))
        advanceUntilIdle()
        assertEquals(listOf("finished-acotar"), viewModel.uiState.value.entries.map { it.key })
    }

    @Test
    fun `a loose thought is saved without a book or page`() = runTest {
        viewModel.addThought(ReadingMood.SMITTEN, "  Hay libros que se viven  ")
        advanceUntilIdle()

        val note = repository.addedNotes.single()
        assertNull(note.bookId)
        assertNull(note.page)
        assertEquals("Hay libros que se viven", note.content)
        assertEquals(clock.millis(), note.createdAt)
    }

    @Test
    fun `an empty thought is not saved`() = runTest {
        viewModel.addThought(null, " ")
        advanceUntilIdle()

        assertTrue(repository.addedNotes.isEmpty())
    }
}
