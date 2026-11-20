package com.nawin.booknook.presentation.ui.share

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.domain.model.ReadingCard
import com.nawin.booknook.domain.service.CardSharer
import com.nawin.booknook.domain.usecase.GetBookByIdUseCase
import com.nawin.booknook.domain.usecase.GetNotesUseCase
import com.nawin.booknook.domain.usecase.GetReadingCardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.YearMonth

/** The four pictures a reader can share from a book page (P20). */
enum class ShareCardType { PROGRESS, LIBRARY_CARD, READING_CARD, REVIEW }

/** Feeds the share screen: what each card shows, which one is picked, and sharing or saving it. */
class ShareViewModel(
    getBookByIdUseCase: GetBookByIdUseCase,
    getNotesUseCase: GetNotesUseCase,
    getReadingCardUseCase: GetReadingCardUseCase,
    preferences: PreferencesDataStore,
    private val cardSharer: CardSharer,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle[BOOK_ID_ARG])

    /** Null until she picks one: then the first card available is shown. */
    private val chosen = MutableStateFlow<ShareCardType?>(null)
    private val status = MutableStateFlow<ShareStatus?>(null)

    private val content = combine(
        getBookByIdUseCase(bookId),
        getNotesUseCase(bookId),
        getReadingCardUseCase(),
        preferences.userPreferences,
    ) { book, notes, card, prefs ->
        ShareContent(
            book = book,
            latestNote = notes.filter { it.content.isNotBlank() }.maxByOrNull { it.createdAt }?.content,
            readingCard = card,
            profile = prefs.profile,
        )
    }

    val uiState: StateFlow<ShareUiState> = combine(content, chosen, status) { content, chosen, status ->
        val types = availableTypes(content.book, content.profile)
        ShareUiState(
            isLoading = false,
            book = content.book,
            latestNote = content.latestNote,
            readingCard = content.readingCard,
            profile = content.profile,
            month = YearMonth.now(clock),
            types = types,
            selected = chosen?.takeIf { it in types } ?: types.first(),
            status = status,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShareUiState())

    fun select(type: ShareCardType) {
        chosen.value = type
        status.value = null
    }

    /** Opens the system share sheet with the picture of the card on screen. */
    fun share(png: ByteArray) {
        if (status.value == ShareStatus.Working) return
        status.value = ShareStatus.Working
        viewModelScope.launch {
            cardSharer.share(png)
            status.value = null
        }
    }

    /** Saves the picture of the card on screen to the gallery. */
    fun save(png: ByteArray) {
        if (status.value == ShareStatus.Working) return
        status.value = ShareStatus.Working
        val name = FILE_PREFIX + uiState.value.selected.name.lowercase() + FILE_SEPARATOR + clock.millis()
        viewModelScope.launch {
            status.value = if (cardSharer.saveToGallery(png, name)) ShareStatus.Saved else ShareStatus.SaveFailed
        }
    }

    /** Called when the picture could not be drawn, or the storage permission was refused. */
    fun saveFailed() {
        status.value = ShareStatus.SaveFailed
    }

    private fun availableTypes(book: Book?, profile: ReaderProfile): List<ShareCardType> = buildList {
        add(ShareCardType.PROGRESS)
        // The library card needs a holder; a reader who skipped the welcome has none.
        if (profile.name.isNotBlank()) add(ShareCardType.LIBRARY_CARD)
        add(ShareCardType.READING_CARD)
        if (book != null && (!book.review.isNullOrBlank() || book.rating > 0f)) add(ShareCardType.REVIEW)
    }

    private data class ShareContent(
        val book: Book?,
        val latestNote: String?,
        val readingCard: ReadingCard,
        val profile: ReaderProfile,
    )

    companion object {
        const val BOOK_ID_ARG = "bookId"
        private const val FILE_PREFIX = "booknook-"
        private const val FILE_SEPARATOR = "-"
    }
}

/** What happened to the last share or save, shown under the buttons. */
sealed interface ShareStatus {
    data object Working : ShareStatus
    data object Saved : ShareStatus
    data object SaveFailed : ShareStatus
}

/**
 * @property latestNote The newest note written about the book, quoted on the progress card.
 * @property month Named on the progress card ("what I'm reading · November").
 * @property types The cards that can be made: the library card needs a holder, the review one a rating or review.
 */
data class ShareUiState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val latestNote: String? = null,
    val readingCard: ReadingCard? = null,
    val profile: ReaderProfile = ReaderProfile(),
    val month: YearMonth? = null,
    val types: List<ShareCardType> = listOf(ShareCardType.PROGRESS),
    val selected: ShareCardType = ShareCardType.PROGRESS,
    val status: ShareStatus? = null,
)
