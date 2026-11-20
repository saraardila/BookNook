package com.nawin.booknook.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Checks that upgrading the database keeps the reader's books and notes. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val file = instrumentation.targetContext.getDatabasePath(TEST_DB)

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = file,
        driver = BundledSQLiteDriver(),
        databaseClass = AppDatabase::class,
    )

    @Test
    fun version1To2KeepsBooksAndNotes() = runTest {
        helper.createDatabase(1).apply {
            execSQL(
                """
                INSERT INTO books (id, title, author, coverUrl, description, publishYear, pageCount, isbn,
                    status, currentPage, rating, review, startDate, finishDate, isFavorite)
                VALUES ('OL17860744W', 'Throne of Glass', 'Sarah J. Maas', NULL, NULL, 2012, 432, NULL,
                    'READING', 97, 4.0, 'Celaena!', 1000, NULL, 1)
                """
            )
            execSQL("INSERT INTO book_notes (bookId, chapter, content, page, createdAt) VALUES ('OL17860744W', '1', 'Endovier', 12, 0)")
            close()
        }

        // Applies the automatic migration and checks the result matches the exported v2 schema.
        helper.runMigrationsAndValidate(2).close()

        val db = Room.databaseBuilder(instrumentation.targetContext, AppDatabase::class.java, file.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        try {
            val book = db.bookDao().getBookById("OL17860744W").first()!!.toDomain()
            assertEquals(97, book.currentPage)
            assertEquals(4f, book.rating)
            assertEquals("Celaena!", book.review)
            assertEquals(true, book.isFavorite)
            assertNull(book.shelf)
            assertEquals("Endovier", db.bookNoteDao().getNotesByBook("OL17860744W").first().single().content)
        } finally {
            db.close()
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
