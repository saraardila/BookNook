package com.nawin.booknook.domain.usecase

import com.nawin.booknook.domain.model.ReadingStreak
import com.nawin.booknook.domain.repository.ReadingLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The reading streak as seen on [today] (an epoch day), updated as pages are logged. */
class GetReadingStreakUseCase(
    private val readingLog: ReadingLogRepository,
) {
    operator fun invoke(today: Long): Flow<ReadingStreak> =
        readingLog.pagesPerDay().map { ReadingStreak.from(it, today) }
}
