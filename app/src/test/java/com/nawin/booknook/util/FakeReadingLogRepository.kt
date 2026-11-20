package com.nawin.booknook.util

import com.nawin.booknook.domain.repository.ReadingLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory reading log that behaves like the DAO: per day and book, never below zero. */
class FakeReadingLogRepository : ReadingLogRepository {

    private val pages = MutableStateFlow<Map<Pair<Long, String>, Int>>(emptyMap())

    override suspend fun logPages(day: Long, bookId: String, pages: Int) {
        val key = day to bookId
        this.pages.value += key to maxOf(0, (this.pages.value[key] ?: 0) + pages)
    }

    override fun pagesPerDay(): Flow<Map<Long, Int>> = pages.map { entries ->
        entries.entries.groupBy({ it.key.first }, { it.value }).mapValues { it.value.sum() }
    }
}
