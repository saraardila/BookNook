package com.nawin.booknook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nawin.booknook.data.local.dao.BookDao
import com.nawin.booknook.data.local.dao.BookNoteDao
import com.nawin.booknook.data.local.entity.BookEntity
import com.nawin.booknook.data.local.entity.BookNoteEntity

@Database(
    entities = [BookEntity::class, BookNoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun bookNoteDao(): BookNoteDao
}