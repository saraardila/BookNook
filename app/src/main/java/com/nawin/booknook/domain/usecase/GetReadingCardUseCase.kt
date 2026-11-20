package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.ReadingCard
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The reading card being filled, updated as books are finished or put down. */
class GetReadingCardUseCase(
    private val repository: BookRepository,
) {
    operator fun invoke(): Flow<ReadingCard> =
        repository.getMyLibrary().map(ReadingCard::current)
}
