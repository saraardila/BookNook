package com.nawin.booknook.data.remote.dto

import com.nawin.booknook.domain.model.Book
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenLibraryResponse(
    @SerialName("docs") val docs: List<BookDto>,
)

/** One work in the search results. */
@Serializable
data class BookDto(
    @SerialName("key") val key: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("author_name") val authorName: List<String>? = null,
    @SerialName("cover_i") val coverId: Long? = null,
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    @SerialName("number_of_pages_median") val pageCount: Int? = null,
    @SerialName("isbn") val isbn: List<String>? = null,
    @SerialName("editions") val editions: EditionsDto? = null,
) {
    /**
     * Maps to the domain. When the edition Open Library picked is in
     * [languageCode] (MARC code, e.g. "spa"), its title and cover are used, so
     * "Throne of Glass" shows as "Trono de cristal" in Spanish. The id stays the
     * work's, so the same book is recognised whatever the language.
     */
    fun toDomain(languageCode: String? = null): Book {
        val edition = editions?.docs?.firstOrNull()
            ?.takeIf { languageCode != null && languageCode in it.language.orEmpty() }
        return Book(
            id = key?.removePrefix(WORKS_PREFIX) ?: title.orEmpty(),
            title = edition?.title ?: title ?: UNKNOWN,
            author = authorName?.firstOrNull() ?: UNKNOWN,
            coverUrl = (edition?.coverId ?: coverId)?.let { coverUrl(it) },
            description = null,
            publishYear = firstPublishYear,
            pageCount = pageCount,
            isbn = isbn?.firstOrNull(),
        )
    }

    private companion object {
        const val WORKS_PREFIX = "/works/"
        // Stored data, not UI copy: the screens show it as it comes.
        const val UNKNOWN = "Unknown"
        fun coverUrl(id: Long) = "https://covers.openlibrary.org/b/id/$id-L.jpg"
    }
}

/** The editions Open Library matched for a work; the first is the one it recommends. */
@Serializable
data class EditionsDto(
    @SerialName("docs") val docs: List<EditionDto> = emptyList(),
)

@Serializable
data class EditionDto(
    @SerialName("key") val key: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("cover_i") val coverId: Long? = null,
    @SerialName("language") val language: List<String>? = null,
)
