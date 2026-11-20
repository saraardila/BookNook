package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.ShelfLicence
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Progress towards each shelf licence, updated as books are finished or filed. */
class GetShelfLicencesUseCase(
    private val repository: BookRepository,
) {
    operator fun invoke(): Flow<List<ShelfLicence>> =
        repository.getMyLibrary().map(ShelfLicence::fromLibrary)
}
