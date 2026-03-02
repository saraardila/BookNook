package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(bookId: String): Flow<List<BookNote>> = repository.getNotesByBook(bookId)
}