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
import org.junit.Assert.assertEquals
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
            api = OpenLibraryApi(client),
            languageProvider = { "en" },
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
        val repository = BookRepositoryImpl(StubBookDao(), StubBookNoteDao(), OpenLibraryApi(client)) { "en" }

        val result = repository.searchBooks("heir of fire")

        assertTrue(result.isFailure)
    }

    // Trimmed real answer to "sarah j maas" with lang=es: the first edition is in
    // Spanish, the second work's only matched edition is Polish.
    private val editionsJson = """
        {"docs":[
          {"key":"/works/OL17860744W","title":"Throne of Glass","author_name":["Sarah J. Maas"],"cover_i":1,
           "editions":{"docs":[{"key":"/books/OL1M","title":"Trono de Cristal","cover_i":2,"language":["spa"]}]}},
          {"key":"/works/OL2W","title":"Heir of Fire","cover_i":3,
           "editions":{"docs":[{"key":"/books/OL2M","title":"Dziedziczka ognia","cover_i":4,"language":["pol"]}]}}
        ]}
    """.trimIndent()

    private fun repositoryInLanguage(language: String, onUrl: (String) -> Unit = {}): BookRepositoryImpl {
        val client = HttpClient(MockEngine { request ->
            onUrl(request.url.toString())
            respond(editionsJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }) { openLibraryDefaults() }
        return BookRepositoryImpl(StubBookDao(), StubBookNoteDao(), OpenLibraryApi(client)) { language }
    }

    @Test
    fun `searches in the app language and uses the edition in that language`() = runTest {
        var url = ""
        val books = repositoryInLanguage("es") { url = it }.searchBooks("sarah j maas").getOrThrow()

        assertTrue("lang not sent: $url", "lang=es" in url)
        assertEquals("OL17860744W", books[0].id) // still the work, so the library recognises it
        assertEquals("Trono de Cristal", books[0].title)
        assertEquals("https://covers.openlibrary.org/b/id/2-L.jpg", books[0].coverUrl)
    }

    @Test
    fun `an edition in another language is ignored`() = runTest {
        val books = repositoryInLanguage("es").searchBooks("sarah j maas").getOrThrow()

        assertEquals("Heir of Fire", books[1].title)
        assertEquals("https://covers.openlibrary.org/b/id/3-L.jpg", books[1].coverUrl)
    }

    @Test
    fun `in English the Spanish edition is not used`() = runTest {
        val books = repositoryInLanguage("en").searchBooks("sarah j maas").getOrThrow()

        assertEquals("Throne of Glass", books[0].title)
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
