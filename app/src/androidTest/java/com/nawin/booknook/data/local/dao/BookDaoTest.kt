package com.nawin.booknook.data.local.dao

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nawin.booknook.data.local.AppDatabase
import com.nawin.booknook.data.local.entity.BookEntity
import com.nawin.booknook.data.local.entity.BookNoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var bookDao: BookDao
    private lateinit var noteDao: BookNoteDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        bookDao = db.bookDao()
        noteDao = db.bookNoteDao()
    }

    @After
    fun tearDown() = db.close()

    private fun book(
        status: String = "WANT_TO_READ",
        currentPage: Int = 0,
        rating: Float = 0f
    ) = BookEntity(
        id = "OL17860744W", title = "Throne of Glass", author = "Sarah J. Maas",
        coverUrl = null, description = null, publishYear = 2012, pageCount = 432,
        isbn = null, status = status, currentPage = currentPage, rating = rating,
        review = null, startDate = null, finishDate = null, isFavorite = false
    )

    @Test
    fun insertingABookAlreadyInTheLibraryKeepsItsProgressAndNotes() = runTest {
        bookDao.insertBook(book())
        bookDao.updateBook(book(status = "READING", currentPage = 97, rating = 4f))
        noteDao.insertNote(
            BookNoteEntity(bookId = "OL17860744W", chapter = "1", content = "Celaena", page = 12, createdAt = 0L)
        )

        // Same book added again from search, with default library fields
        bookDao.insertBook(book())

        val stored = bookDao.getBookById("OL17860744W").first()!!
        assertEquals("READING", stored.status)
        assertEquals(97, stored.currentPage)
        assertEquals(4f, stored.rating)
        assertEquals(1, noteDao.getNotesByBook("OL17860744W").first().size)
    }
}
