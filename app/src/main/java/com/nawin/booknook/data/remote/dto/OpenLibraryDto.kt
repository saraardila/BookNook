package com.nawin.booknook.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenLibraryResponse(
    @SerialName("docs") val docs: List<BookDto>
)

@Serializable
data class BookDto(
    @SerialName("key")          val key: String? = null,
    @SerialName("title")        val title: String? = null,
    @SerialName("author_name")  val authorName: List<String>? = null,
    @SerialName("cover_i")      val coverId: Long? = null,
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    @SerialName("number_of_pages_median") val pageCount: Int? = null,
    @SerialName("isbn")         val isbn: List<String>? = null
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
