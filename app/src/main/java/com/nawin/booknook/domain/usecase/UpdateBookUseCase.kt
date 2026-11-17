package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.repository.BookRepository

class UpdateBookUseCase(
    private val repository: BookRepository
) {
    suspend operator fun invoke(book: Book) = repository.updateBook(book)
}