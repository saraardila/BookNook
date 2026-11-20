package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.repository.ReadingLogRepository
import kotlinx.coroutines.flow.Flow

/** Pages read per day (epoch day → pages) across all books, for the calendar. */
class GetPagesPerDayUseCase(
    private val readingLog: ReadingLogRepository,
) {
    operator fun invoke(): Flow<Map<Long, Int>> = readingLog.pagesPerDay()
}
