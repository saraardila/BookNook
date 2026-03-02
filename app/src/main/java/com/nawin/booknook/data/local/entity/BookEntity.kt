package com.nawin.booknook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val description: String?,
    val publishYear: Int?,
    val pageCount: Int?,
    val isbn: String?,
    val status: String,
    val currentPage: Int,
    val rating: Float,
    val review: String?,
    val startDate: Long?,
    val finishDate: Long?,
    val isFavorite: Boolean
) {
    fun toDomain() = Book(
        id = id,
        title = title,
        author = author,
        coverUrl = coverUrl,
        description = description,
        publishYear = publishYear,
        pageCount = pageCount,
        isbn = isbn,
        status = ReadingStatus.valueOf(status),
        currentPage = currentPage,
        rating = rating,
        review = review,
        startDate = startDate,
        finishDate = finishDate,
        isFavorite = isFavorite
    )
}

fun Book.toEntity() = BookEntity(
    id = id,
    title = title,
    author = author,
    coverUrl = coverUrl,
    description = description,
    publishYear = publishYear,
    pageCount = pageCount,
    isbn = isbn,
    status = status.name,
    currentPage = currentPage,
    rating = rating,
    review = review,
    startDate = startDate,
    finishDate = finishDate,
    isFavorite = isFavorite
)