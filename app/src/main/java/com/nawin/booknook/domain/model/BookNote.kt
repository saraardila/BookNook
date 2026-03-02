package com.nawin.booknook.domain.model

data class BookNote(
    val id: Long = 0,
    val bookId: String,
    val chapter: String?,
    val content: String,
    val page: Int?,
    val createdAt: Long = System.currentTimeMillis()
)