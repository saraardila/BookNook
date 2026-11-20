package com.nawin.booknook.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.nawin.booknook.data.local.entity.DailyPages
import kotlinx.coroutines.flow.Flow

/** Access to the daily reading log (see [com.nawin.booknook.data.local.entity.ReadingLogEntity]). */
@Dao
interface ReadingLogDao {

    /**
     * Adds [pages] to what was already logged for [bookId] on [day], creating the row if
     * needed. [pages] may be negative (a correction); the total is kept at zero or above.
     */
    @Query(
        """
        INSERT INTO reading_log (day, bookId, pagesRead) VALUES (:day, :bookId, MAX(0, :pages))
        ON CONFLICT (day, bookId) DO UPDATE SET pagesRead = MAX(0, pagesRead + :pages)
        """
    )
    suspend fun addPages(day: Long, bookId: String, pages: Int)

    /** Total pages read per day, oldest first. */
    @Query("SELECT day, SUM(pagesRead) AS pagesRead FROM reading_log GROUP BY day ORDER BY day")
    fun observePagesPerDay(): Flow<List<DailyPages>>
}
