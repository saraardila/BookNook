package com.nawin.booknook.domain.repository

import kotlinx.coroutines.flow.Flow

/** Daily record of pages read, the source of the reading streak. Days are epoch days. */
interface ReadingLogRepository {

    /**
     * Adds [pages] to what was logged for [bookId] on [day]. A negative value undoes
     * pages moved forward by mistake that day; the total never goes below zero.
     */
    suspend fun logPages(day: Long, bookId: String, pages: Int)

    /** Pages read per day across all books. */
    fun pagesPerDay(): Flow<Map<Long, Int>>
}
