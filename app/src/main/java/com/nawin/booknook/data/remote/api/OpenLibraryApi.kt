package com.nawin.booknook.data.remote.api

import com.nawin.booknook.data.remote.dto.OpenLibraryResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Open Library's search endpoint (`search.json`). */
class OpenLibraryApi(private val client: HttpClient) {

    /**
     * Searches works matching [query]. With [language] (ISO 639-1), Open Library
     * ranks and picks, for each work, an edition in that language when there is
     * one; it comes back in the `editions` field.
     */
    suspend fun searchBooks(
        query: String,
        language: String? = null,
        limit: Int = DEFAULT_LIMIT,
        fields: String = DEFAULT_FIELDS,
    ): OpenLibraryResponse =
        client.get("search.json") {
            parameter("q", query)
            language?.let { parameter("lang", it) }
            parameter("limit", limit)
            parameter("fields", fields)
        }.body()

    companion object {
        const val DEFAULT_LIMIT = 20
        const val DEFAULT_FIELDS = "key,title,author_name,cover_i,first_publish_year,number_of_pages_median,isbn," +
            "editions,editions.key,editions.title,editions.cover_i,editions.language"
    }
}
