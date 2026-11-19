package com.nawin.booknook.data.repository

import com.nawin.booknook.data.local.dao.BookDao
import com.nawin.booknook.data.local.dao.BookNoteDao
import com.nawin.booknook.data.local.entity.toEntity
import com.nawin.booknook.data.locale.LanguageProvider
import com.nawin.booknook.data.remote.OpenLibraryLanguage
import com.nawin.booknook.data.remote.api.OpenLibraryApi
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val bookNoteDao: BookNoteDao,
    private val api: OpenLibraryApi,
    private val languageProvider: LanguageProvider,
) : BookRepository {

    override suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            // Results come in the app language when Open Library has an edition in it.
            val language = languageProvider.current()
            val response = api.searchBooks(query, language = language)
            val marcCode = OpenLibraryLanguage.marcCode(language)
            Result.success(response.docs.map { it.toDomain(marcCode) })
        } catch (e: CancellationException) {
            // A cancelled search is not a failure to report: let it unwind the coroutine
            throw e
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