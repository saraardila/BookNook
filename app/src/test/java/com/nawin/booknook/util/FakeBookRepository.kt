package com.nawin.booknook.util

import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/** Lets a test decide what each query returns and how long it takes. */
class FakeBookRepository(
    var onSearch: suspend (String) -> Result<List<Book>> = { Result.success(emptyList()) }
) : BookRepository {

    val addedBooks = mutableListOf<Book>()

    /** What getMyLibrary() emits; set its value to change the library during a test. */
    val library = MutableStateFlow<List<Book>>(emptyList())

    override suspend fun searchBooks(query: String): Result<List<Book>> = onSearch(query)

    override suspend fun addBookToLibrary(book: Book) {
        addedBooks += book
        library.value = library.value + book
    }

    override fun getMyLibrary(): Flow<List<Book>> = library
    override fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>> = flowOf(emptyList())
    override fun getBookById(id: String): Flow<Book?> = flowOf(null)
    override suspend fun updateBook(book: Book) = Unit
    override suspend fun deleteBook(bookId: String) = Unit
    override fun getNotesByBook(bookId: String): Flow<List<BookNote>> = flowOf(emptyList())
    override suspend fun addNote(note: BookNote) = Unit
    override suspend fun deleteNote(noteId: Long) = Unit
}

fun book(id: String, title: String = id) = Book(
    id = id, title = title, author = "Sarah J. Maas",
    coverUrl = null, description = null, publishYear = null, pageCount = null, isbn = null
)
