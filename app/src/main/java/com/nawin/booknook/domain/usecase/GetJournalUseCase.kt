package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.JournalEntry
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** The reading diary: finished and put-down books plus every note, newest first. */
class GetJournalUseCase(
    private val repository: BookRepository,
) {
    operator fun invoke(): Flow<List<JournalEntry>> =
        combine(repository.getMyLibrary(), repository.getAllNotes(), JournalEntry::from)
}
