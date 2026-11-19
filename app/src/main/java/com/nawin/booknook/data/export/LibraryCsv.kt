package com.nawin.booknook.data.export

import com.nawin.booknook.domain.model.Book
import java.time.Instant
import java.time.ZoneId

/**
 * The library as CSV (RFC 4180): one row per book under an English header, so
 * spreadsheets and other apps can read it whatever the app language. Dates are
 * ISO calendar days in [zone]; empty fields are left blank.
 */
internal object LibraryCsv {

    private val HEADER = listOf(
        "title", "author", "status", "current_page", "page_count", "rating", "favourite",
        "start_date", "finish_date", "publish_year", "isbn", "review",
    )

    fun format(books: List<Book>, zone: ZoneId): String = buildString {
        appendRow(HEADER)
        books.forEach { book ->
            appendRow(
                listOf(
                    book.title,
                    book.author,
                    book.status.name,
                    book.currentPage.toString(),
                    book.pageCount?.toString().orEmpty(),
                    book.rating.takeIf { it > 0f }?.toString().orEmpty(),
                    book.isFavorite.toString(),
                    book.startDate?.let { isoDay(it, zone) }.orEmpty(),
                    book.finishDate?.let { isoDay(it, zone) }.orEmpty(),
                    book.publishYear?.toString().orEmpty(),
                    book.isbn.orEmpty(),
                    book.review.orEmpty(),
                )
            )
        }
    }

    private fun StringBuilder.appendRow(fields: List<String>) {
        fields.joinTo(this, separator = ",", transform = ::escape)
        append(LINE_END)
    }

    /** Quotes a field when it holds a comma, a quote or a line break, doubling inner quotes. */
    private fun escape(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }

    private fun isoDay(epochMillis: Long, zone: ZoneId): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().toString()

    private const val LINE_END = "\r\n"
}
