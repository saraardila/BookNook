package com.nawin.booknook.domain.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val description: String?,
    val publishYear: Int?,
    val pageCount: Int?,
    val isbn: String?,
    // Estado en tu biblioteca
    val status: ReadingStatus = ReadingStatus.WANT_TO_READ,
    val currentPage: Int = 0,
    val rating: Float = 0f,
    val review: String? = null,
    val startDate: Long? = null,
    val finishDate: Long? = null,
    val isFavorite: Boolean = false
)

enum class ReadingStatus {
    WANT_TO_READ,
    READING,
    FINISHED,
    DNF // Did Not Finish
}