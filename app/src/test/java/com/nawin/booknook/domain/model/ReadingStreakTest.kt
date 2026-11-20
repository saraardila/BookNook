package com.nawin.booknook.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingStreakTest {

    private val today = 20_000L

    @Test
    fun `no reading gives an empty streak`() {
        assertEquals(ReadingStreak(), ReadingStreak.from(emptyMap(), today))
    }

    @Test
    fun `consecutive days up to today count`() {
        val streak = ReadingStreak.from(mapOf(today to 10, today - 1 to 20, today - 2 to 5, today - 4 to 30), today)

        assertEquals(3, streak.current)
        assertEquals(setOf(today, today - 1, today - 2, today - 4), streak.daysRead)
    }

    @Test
    fun `not having read yet today keeps yesterday's streak alive`() {
        val streak = ReadingStreak.from(mapOf(today - 1 to 10, today - 2 to 10), today)

        assertEquals(2, streak.current)
    }

    @Test
    fun `missing yesterday and today breaks the streak`() {
        val streak = ReadingStreak.from(mapOf(today - 2 to 10, today - 3 to 10), today)

        assertEquals(0, streak.current)
        assertEquals(2, streak.record)
    }

    @Test
    fun `a day under the minimum pages does not count`() {
        val streak = ReadingStreak.from(
            mapOf(today to ReadingStreak.MIN_PAGES_PER_DAY - 1, today - 1 to ReadingStreak.MIN_PAGES_PER_DAY),
            today,
        )

        assertEquals(1, streak.current)
        assertEquals(setOf(today - 1), streak.daysRead)
    }

    @Test
    fun `the record is the longest run ever`() {
        val pages = (today - 20..today - 14).associateWith { 10 } + mapOf(today to 10)

        val streak = ReadingStreak.from(pages, today)

        assertEquals(1, streak.current)
        assertEquals(7, streak.record)
    }
}
