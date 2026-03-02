package com.nawin.booknook.data.repository

import com.nawin.booknook.data.local.dao.BookDao
import com.nawin.booknook.data.local.dao.BookNoteDao
import com.nawin.booknook.data.local.entity.toEntity
import com.nawin.booknook.data.remote.api.OpenLibraryApi
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val bookNoteDao: BookNoteDao,
    private val api: OpenLibraryApi
) : BookRepository {

    override suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            val response = api.searchBooks(query)
            Result.success(response.docs.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMyLibrary(): Flow<List<Book>> =
        bookDao.getAllBooks().map { list -> list.map { it.toDomain() } }

    override fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>> =
        bookDao.getBooksByStatus(status.name).map { list -> list.map { it.toDomain() } }

    override fun getBookById(id: String): Flow<Book?> =
        bookDao.getBookById(id).map { it?.toDomain() }

    override suspend fun addBookToLibrary(book: Book) =
        bookDao.insertBook(book.toEntity())

    override suspend fun updateBook(book: Book) =
        bookDao.updateBook(book.toEntity())

    override suspend fun deleteBook(bookId: String) =
        bookDao.deleteBook(bookId)

    override fun getNotesByBook(bookId: String): Flow<List<BookNote>> =
        bookNoteDao.getNotesByBook(bookId).map { list -> list.map { it.toDomain() } }

    override suspend fun addNote(note: BookNote) =
        bookNoteDao.insertNote(note.toEntity())

    override suspend fun deleteNote(noteId: Long) =
        bookNoteDao.deleteNote(noteId)
}