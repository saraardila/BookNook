package com.nawin.booknook.data.repository

import com.nawin.booknook.data.local.dao.ReadingLogDao
import com.nawin.booknook.domain.repository.ReadingLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingLogRepositoryImpl(
    private val dao: ReadingLogDao,
) : ReadingLogRepository {

    override suspend fun logPages(day: Long, bookId: String, pages: Int) =
        dao.addPages(day, bookId, pages)

    override fun pagesPerDay(): Flow<Map<Long, Int>> =
        dao.observePagesPerDay().map { rows -> rows.associate { it.day to it.pagesRead } }
}
