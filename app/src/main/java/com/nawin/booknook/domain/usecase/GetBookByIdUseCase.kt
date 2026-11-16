package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class GetBookByIdUseCase(
    private val repository: BookRepository
) {
    operator fun invoke(id: String): Flow<Book?> = repository.getBookById(id)
}