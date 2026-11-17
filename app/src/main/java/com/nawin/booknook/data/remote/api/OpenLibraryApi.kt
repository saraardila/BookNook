package com.nawin.booknook.data.remote.api

import com.nawin.booknook.data.remote.dto.OpenLibraryResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class OpenLibraryApi(private val client: HttpClient) {

    suspend fun searchBooks(
        query: String,
        limit: Int = 20,
        fields: String = "key,title,author_name,cover_i,first_publish_year,number_of_pages_median,isbn"
    ): OpenLibraryResponse =
        client.get("search.json") {
            parameter("q", query)
            parameter("limit", limit)
            parameter("fields", fields)
        }.body()
}
