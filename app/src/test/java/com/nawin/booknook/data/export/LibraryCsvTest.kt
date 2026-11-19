package com.nawin.booknook.data.export

import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.util.book
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class LibraryCsvTest {

    private val zone = ZoneId.of("Europe/Madrid")

    private val header =
        "title,author,status,current_page,page_count,rating,favourite,start_date,finish_date,publish_year,isbn,review"

    @Test
    fun `an empty library is just the header`() {
        assertEquals("$header\r\n", LibraryCsv.format(emptyList(), zone))
    }

    @Test
    fun `commas, quotes and line breaks are quoted`() {
        val review = "Loved it, \"truly\".\nWould reread."
        val csv = LibraryCsv.format(listOf(book("1", title = "Fourth Wing").copy(review = review)), zone)

        assertEquals(
            "$header\r\nFourth Wing,Sarah J. Maas,WANT_TO_READ,0,,,false,,,,,\"Loved it, \"\"truly\"\".\nWould reread.\"\r\n",
            csv,
        )
    }

    @Test
    fun `dates are calendar days in the given zone`() {
        // 23:30 UTC on 31 Dec is already 1 Jan in Madrid.
        val lateNewYearsEve = LocalDate.of(2025, 12, 31).atTime(23, 30)
            .atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val finished = book("1").copy(
            status = ReadingStatus.FINISHED,
            currentPage = 320,
            pageCount = 320,
            rating = 4.5f,
            isFavorite = true,
            startDate = lateNewYearsEve,
            finishDate = lateNewYearsEve,
            publishYear = 2023,
            isbn = "9781649374042",
        )

        assertEquals(
            "$header\r\n1,Sarah J. Maas,FINISHED,320,320,4.5,true,2026-01-01,2026-01-01,2023,9781649374042,\r\n",
            LibraryCsv.format(listOf(finished), zone),
        )
    }
}
