package com.nawin.booknook.data.export

import android.content.Context
import androidx.core.net.toUri
import com.nawin.booknook.domain.repository.BookRepository
import com.nawin.booknook.domain.service.LibraryExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.ZoneId

/**
 * Writes the library as UTF-8 CSV to a document the reader picked with the
 * system "save as" screen, so no storage permission is needed.
 */
class CsvLibraryExporter(
    private val context: Context,
    private val repository: BookRepository,
) : LibraryExporter {

    override suspend fun export(destination: String): Int {
        val books = repository.getMyLibrary().first()
        val csv = LibraryCsv.format(books, ZoneId.systemDefault())
        withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openOutputStream(destination.toUri())
                ?: throw IOException("Cannot open $destination")
            stream.use {
                // The byte order mark lets Excel detect UTF-8, so accents show correctly.
                it.write(UTF8_BOM)
                it.write(csv.toByteArray(Charsets.UTF_8))
            }
        }
        return books.size
    }

    private companion object {
        val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    }
}
