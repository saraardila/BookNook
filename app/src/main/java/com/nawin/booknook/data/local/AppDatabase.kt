package com.nawin.booknook.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.nawin.booknook.data.local.dao.BookDao
import com.nawin.booknook.data.local.dao.BookNoteDao
import com.nawin.booknook.data.local.dao.ReadingLogDao
import com.nawin.booknook.data.local.entity.BookEntity
import com.nawin.booknook.data.local.entity.BookNoteEntity
import com.nawin.booknook.data.local.entity.ReadingLogEntity

/**
 * Local library database.
 *
 * Every version bump needs a migration: there is no destructive fallback, so a
 * missing one fails loudly instead of wiping the reader's books. Room generates
 * automatic migrations by diffing the schemas exported to `app/schemas/`.
 *
 * - v2: `books.shelf`, the `reading_log` table and an index on `book_notes.bookId`.
 * - v3: `book_notes.mood`, and `book_notes.bookId` may be null for loose thoughts.
 */
@Database(
    entities = [BookEntity::class, BookNoteEntity::class, ReadingLogEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun bookNoteDao(): BookNoteDao
    abstract fun readingLogDao(): ReadingLogDao
}
