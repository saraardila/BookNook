package com.nawin.booknook.data.repository

import com.nawin.booknook.data.local.dao.BookDao
import com.nawin.booknook.data.local.dao.BookNoteDao
import com.nawin.booknook.data.remote.api.OpenLibraryApi
import com.nawin.booknook.di.openLibraryDefaults
import com.nawin.booknook.domain.model.Book
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BookRepositoryImplTest {

    private fun repositoryWithApiTaking(millis: Long): BookRepositoryImpl {
        val client = HttpClient(MockEngine { request ->
            delay(millis)
            respond(
                """{"docs":[]}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }) { openLibraryDefaults() }
        return BookRepositoryImpl(
            bookDao = StubBookDao(),
            bookNoteDao = StubBookNoteDao(),
            api = OpenLibraryApi(client)
        )
    }

    @Test
    fun `a cancelled search does not come back as a failed Result`() = runTest {
        val repository = repositoryWithApiTaking(3_000)
        var result: Result<List<Book>>? = null

        val job = launch { result = repository.searchBooks("heir of fire") }
        advanceTimeBy(100) // request in flight
        job.cancel()
        advanceTimeBy(5_000)

        assertNull("searchBooks reported the cancellation as a result", result)
        assertTrue(job.isCancelled)
    }

    @Test
    fun `network errors still come back as a failed Result`() = runTest {
        val client = HttpClient(MockEngine { respond("", HttpStatusCode.ServiceUnavailable) }) {
            openLibraryDefaults()
        }
        val repository = BookRepositoryImpl(StubBookDao(), StubBookNoteDao(), OpenLibraryApi(client))

        val result = repository.searchBooks("heir of fire")

        assertTrue(result.isFailure)
    }
}

private class StubBookDao : BookDao {
    override fun getAllBooks() = throw NotImplementedError()
    override fun getBooksByStatus(status: String) = throw NotImplementedError()
    override fun getBookById(id: String) = throw NotImplementedError()
    override suspend fun insertBook(book: com.nawin.booknook.data.local.entity.BookEntity) = Unit
    override suspend fun updateBook(book: com.nawin.booknook.data.local.entity.BookEntity) = Unit
    override suspend fun deleteBook(bookId: String) = Unit
    override fun getCurrentlyReading() = throw NotImplementedError()
    override fun getFavorites() = throw NotImplementedError()
}

private class StubBookNoteDao : BookNoteDao {
    override fun getNotesByBook(bookId: String) = throw NotImplementedError()
    override suspend fun insertNote(note: com.nawin.booknook.data.local.entity.BookNoteEntity) = Unit
    override suspend fun deleteNote(noteId: Long) = Unit
}
