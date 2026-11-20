package com.nawin.booknook.data.local.entity

import androidx.room.Entity

/**
 * Pages read of one book on one day, the raw data behind the reading streak.
 *
 * There is deliberately no foreign key to `books`: removing a book from the library
 * must not erase the days already read. The primary key starts with [day], so it also
 * serves the per-day queries.
 *
 * @property day Day as [java.time.LocalDate.toEpochDay], in the reader's time zone.
 * @property pagesRead Pages moved forward on that day, accumulated across updates.
 */
@Entity(tableName = "reading_log", primaryKeys = ["day", "bookId"])
data class ReadingLogEntity(
    val day: Long,
    val bookId: String,
    val pagesRead: Int,
)

/** Pages read on one day across all books. */
data class DailyPages(
    val day: Long,
    val pagesRead: Int,
)
