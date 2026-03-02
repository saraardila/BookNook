package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.repository.BookRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(note: BookNote) = repository.addNote(note)
}