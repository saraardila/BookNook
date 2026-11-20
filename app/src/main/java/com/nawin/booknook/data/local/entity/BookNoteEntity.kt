package com.nawin.booknook.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nawin.booknook.domain.model.BookNote
import com.nawin.booknook.domain.model.ReadingMood

/**
 * A row of `book_notes`. A null [bookId] is a loose thought: SQLite does not check
 * foreign keys on null values, so it needs no book.
 */
@Entity(
    tableName = "book_notes",
    foreignKeys = [ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["id"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )],
    // Without it every change to `books` scans the whole notes table to apply the cascade.
    indices = [Index("bookId")],
)
data class BookNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String?,
    val chapter: String?,
    val content: String,
    val page: Int?,
    val createdAt: Long,
    /** [ReadingMood.name]; unknown names read back as no mood. */
    val mood: String? = null,
) {
    fun toDomain() = BookNote(
        id = id,
        bookId = bookId,
        chapter = chapter,
        content = content,
        page = page,
        mood = ReadingMood.fromName(mood),
        createdAt = createdAt
    )
}

fun BookNote.toEntity() = BookNoteEntity(
    id = id,
    bookId = bookId,
    chapter = chapter,
    content = content,
    page = page,
    createdAt = createdAt,
    mood = mood?.name,
)
