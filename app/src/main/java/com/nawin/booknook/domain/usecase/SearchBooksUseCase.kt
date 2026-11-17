package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.repository.BookRepository

class SearchBooksUseCase(
    private val repository: BookRepository
) {
    suspend operator fun invoke(query: String): Result<List<Book>> {
        if (query.isBlank()) return Result.success(emptyList())
        return repository.searchBooks(query)
    }
}