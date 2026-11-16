package com.nawin.booknook.data.remote.api

import com.nawin.booknook.di.openLibraryDefaults
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenLibraryApiTest {

    // Trimmed real response: the second doc lacks most fields and both carry keys we don't map
    private val searchJson = """
        {
          "numFound": 2,
          "docs": [
            {
              "key": "/works/OL17860744W",
              "title": "Throne of Glass",
              "author_name": ["Sarah J. Maas"],
              "cover_i": 7895383,
              "first_publish_year": 2012,
              "number_of_pages_median": 432,
              "isbn": ["9781619630345"],
              "ebook_access": "no_ebook"
            },
            {
              "key": "/works/OL1W",
              "title": "Untitled draft",
              "language": ["eng"]
            }
          ]
        }
    """.trimIndent()

    private fun apiRespondingWith(handler: MockRequestHandler) =
        OpenLibraryApi(HttpClient(MockEngine(handler)) { openLibraryDefaults() })

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun `searchBooks sends query, limit and fields to search json`() = runTest {
        var requestedUrl = ""
        val api = apiRespondingWith { request ->
            requestedUrl = request.url.toString()
            respond(searchJson, HttpStatusCode.OK, jsonHeaders)
        }

        api.searchBooks("throne of glass", limit = 5, fields = "key,title")

        assertEquals(
            "https://openlibrary.org/search.json?q=throne+of+glass&limit=5&fields=key%2Ctitle",
            requestedUrl
        )
    }

    @Test
    fun `searchBooks parses docs with missing and unknown fields`() = runTest {
        val api = apiRespondingWith { respond(searchJson, HttpStatusCode.OK, jsonHeaders) }

        val docs = api.searchBooks("throne").docs

        assertEquals(2, docs.size)
        assertEquals("Throne of Glass", docs[0].title)
        assertEquals(listOf("Sarah J. Maas"), docs[0].authorName)
        assertEquals(7895383L, docs[0].coverId)
        assertEquals(432, docs[0].pageCount)
        assertNull(docs[1].authorName)
        assertNull(docs[1].coverId)
    }

    @Test
    fun `toDomain maps ids, cover url and defaults`() = runTest {
        val api = apiRespondingWith { respond(searchJson, HttpStatusCode.OK, jsonHeaders) }

        val books = api.searchBooks("throne").docs.map { it.toDomain() }

        assertEquals("OL17860744W", books[0].id)
        assertEquals("https://covers.openlibrary.org/b/id/7895383-L.jpg", books[0].coverUrl)
        assertEquals("9781619630345", books[0].isbn)
        assertEquals("Unknown", books[1].author)
        assertNull(books[1].coverUrl)
    }

    @Test(expected = ServerResponseException::class)
    fun `searchBooks throws on server errors`() = runTest {
        val api = apiRespondingWith { respond("", HttpStatusCode.ServiceUnavailable) }

        api.searchBooks("throne")
    }
}
