package com.nawin.booknook.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.nawin.booknook.domain.model.BookNote

@Entity(
    tableName = "book_notes",
    foreignKeys = [ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["id"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class BookNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapter: String?,
    val content: String,
    val page: Int?,
    val createdAt: Long
) {
    fun toDomain() = BookNote(
        id = id,
        bookId = bookId,
        chapter = chapter,
        content = content,
        page = page,
        createdAt = createdAt
    )
}

fun BookNote.toEntity() = BookNoteEntity(
    id = id,
    bookId = bookId,
    chapter = chapter,
    content = content,
    page = page,
    createdAt = createdAt
)