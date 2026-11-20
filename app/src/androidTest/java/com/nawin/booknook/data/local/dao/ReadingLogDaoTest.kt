package com.nawin.booknook.data.local.dao

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nawin.booknook.data.local.AppDatabase
import com.nawin.booknook.data.local.entity.DailyPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingLogDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ReadingLogDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        dao = db.readingLogDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun pagesAddUpPerDayAcrossUpdatesAndBooks() = runTest {
        dao.addPages(day = 20_000, bookId = "a", pages = 10)
        dao.addPages(day = 20_000, bookId = "a", pages = 5)
        dao.addPages(day = 20_000, bookId = "b", pages = 3)
        dao.addPages(day = 19_999, bookId = "a", pages = 7)

        assertEquals(
            listOf(DailyPages(19_999, 7), DailyPages(20_000, 18)),
            dao.observePagesPerDay().first(),
        )
    }
}
