package com.nawin.booknook.domain.repository

import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingStatus
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    // --- Remote ---
    suspend fun searchBooks(query: String): Result<List<Book>>

    // --- Local ---
    fun getMyLibrary(): Flow<List<Book>>
    fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>>
    fun getBookById(id: String): Flow<Book?>
    suspend fun addBookToLibrary(book: Book)
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(bookId: String)

    // --- Notes ---
    fun getNotesByBook(bookId: String): Flow<List<BookNote>>
    suspend fun addNote(note: BookNote)
    suspend fun deleteNote(noteId: Long)
}