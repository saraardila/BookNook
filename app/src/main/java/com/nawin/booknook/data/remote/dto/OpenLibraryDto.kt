package com.nawin.booknook.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenLibraryResponse(
    @SerializedName("docs") val docs: List<BookDto>
)

data class BookDto(
    @SerializedName("key")          val key: String?,
    @SerializedName("title")        val title: String?,
    @SerializedName("author_name")  val authorName: List<String>?,
    @SerializedName("cover_i")      val coverId: Long?,
    @SerializedName("first_publish_year") val firstPublishYear: Int?,
    @SerializedName("number_of_pages_median") val pageCount: Int?,
    @SerializedName("isbn")         val isbn: List<String>?
) {
    fun toDomain() = com.nawin.booknook.domain.model.Book(
        id = key?.replace("/works/", "") ?: title.orEmpty(),
        title = title ?: "Unknown",
        author = authorName?.firstOrNull() ?: "Unknown",
        coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-L.jpg" },
        description = null,
        publishYear = firstPublishYear,
        pageCount = pageCount,
        isbn = isbn?.firstOrNull()
    )
}