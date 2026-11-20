package com.nawin.booknook.presentation.ui.share

import androidx.lifecycle.SavedStateHandle
import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.service.CardSharer
import com.nawin.booknook.domain.usecase.GetBookByIdUseCase
import com.nawin.booknook.domain.usecase.GetNotesUseCase
import com.nawin.booknook.domain.usecase.GetReadingCardUseCase
import com.nawin.booknook.util.FakeBookRepository
import com.nawin.booknook.util.InMemoryPreferencesDataStore
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
import java.time.Instant
import java.time.ZoneOffset

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ShareViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookRepository()
    private val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())
    private val sharer = FakeCardSharer()
    private val clock = Clock.fixed(Instant.parse("2026-11-20T10:00:00Z"), ZoneOffset.UTC)

    private val viewModel by lazy {
        ShareViewModel(
            GetBookByIdUseCase(repository),
            GetNotesUseCase(repository),
            GetReadingCardUseCase(repository),
            preferences,
            sharer,
            clock,
            SavedStateHandle(mapOf(ShareViewModel.BOOK_ID_ARG to "tog")),
        )
    }

    @Test
    fun `only the cards with something to show can be picked`() = runTest {
        repository.library.value = listOf(book("tog"))
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        // No reader name and no review yet: progress and the reading card only.
        assertEquals(listOf(ShareCardType.PROGRESS, ShareCardType.READING_CARD), viewModel.uiState.value.types)

        preferences.saveReaderName("Sara")
        repository.library.value = listOf(book("tog").copy(rating = 4f))
        advanceUntilIdle()
        assertEquals(ShareCardType.entries, viewModel.uiState.value.types)
    }

    @Test
    fun `the progress card quotes the newest note with text`() = runTest {
        repository.library.value = listOf(book("tog"))
        repository.notes.value = listOf(
            BookNote(1, "tog", null, "cap. 12", null, null, 100),
            BookNote(2, "tog", null, "cap. 31 · ¡no puedo parar!", null, null, 300),
            BookNote(3, "tog", null, " ", null, null, 400),
            BookNote(4, "other", null, "otro libro", null, null, 500),
        )
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        assertEquals("cap. 31 · ¡no puedo parar!", viewModel.uiState.value.latestNote)
    }

    @Test
    fun `a card that is no longer available falls back to the first one`() = runTest {
        repository.library.value = listOf(book("tog").copy(review = "Me encantó"))
        viewModel.uiState.launchIn(backgroundScope)
        viewModel.select(ShareCardType.REVIEW)
        advanceUntilIdle()
        assertEquals(ShareCardType.REVIEW, viewModel.uiState.value.selected)

        repository.library.value = listOf(book("tog"))
        advanceUntilIdle()
        assertEquals(ShareCardType.PROGRESS, viewModel.uiState.value.selected)
    }

    @Test
    fun `saving names the picture after the card and reports the result`() = runTest {
        repository.library.value = listOf(book("tog"))
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.save(byteArrayOf(1))
        advanceUntilIdle()
        assertEquals("booknook-progress-${clock.millis()}", sharer.savedNames.single())
        assertEquals(ShareStatus.Saved, viewModel.uiState.value.status)

        sharer.saveSucceeds = false
        viewModel.save(byteArrayOf(1))
        advanceUntilIdle()
        assertEquals(ShareStatus.SaveFailed, viewModel.uiState.value.status)
    }

    @Test
    fun `sharing hands the picture over and clears the status`() = runTest {
        repository.library.value = listOf(book("tog"))
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.share(byteArrayOf(7))
        advanceUntilIdle()
        assertTrue(sharer.shared.single().contentEquals(byteArrayOf(7)))
        assertNull(viewModel.uiState.value.status)
    }

    private class FakeCardSharer : CardSharer {
        val shared = mutableListOf<ByteArray>()
        val savedNames = mutableListOf<String>()
        var saveSucceeds = true

        override suspend fun share(png: ByteArray) {
            shared += png
        }

        override suspend fun saveToGallery(png: ByteArray, fileName: String): Boolean {
            savedNames += fileName
            return saveSucceeds
        }
    }
}
