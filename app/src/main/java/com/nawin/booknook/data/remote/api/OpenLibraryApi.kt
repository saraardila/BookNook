package com.nawin.booknook.data.remote.api

import com.nawin.booknook.data.remote.dto.OpenLibraryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "key,title,author_name,cover_i,first_publish_year,number_of_pages_median,isbn"
    ): OpenLibraryResponse
}