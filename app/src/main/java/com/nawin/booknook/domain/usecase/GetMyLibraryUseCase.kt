package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyLibraryUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(): Flow<List<Book>> = repository.getMyLibrary()
    operator fun invoke(status: ReadingStatus): Flow<List<Book>> = repository.getBooksByStatus(status)
}