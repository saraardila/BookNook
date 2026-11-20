package com.nawin.booknook.data.local.dao

import androidx.room.*
import com.nawin.booknook.data.local.entity.BookNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookNoteDao {

    @Query("SELECT * FROM book_notes WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getNotesByBook(bookId: String): Flow<List<BookNoteEntity>>

    /** Every note and loose thought, newest first, for the diary. */
    @Query("SELECT * FROM book_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<BookNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: BookNoteEntity)

    @Query("DELETE FROM book_notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)
}