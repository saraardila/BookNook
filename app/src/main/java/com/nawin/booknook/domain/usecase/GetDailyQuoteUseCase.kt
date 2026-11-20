package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.DailyQuote
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Today's quote for the home screen, picked from the reader's own notes. */
class GetDailyQuoteUseCase(
    private val repository: BookRepository,
) {
    operator fun invoke(epochDay: Long): Flow<DailyQuote?> =
        combine(repository.getAllNotes(), repository.getMyLibrary()) { notes, books ->
            DailyQuote.pick(notes, books, epochDay)
        }
}
