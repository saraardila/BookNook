package com.nawin.booknook.data.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderTimingTest {

    private val madrid = ZoneId.of("Europe/Madrid")

    private fun at(month: Int, day: Int, hour: Int, minute: Int) =
        LocalDateTime.of(2026, month, day, hour, minute).atZone(madrid)

    @Test
    fun `a time still ahead today fires today`() {
        assertEquals(Duration.ofMinutes(150), delayUntilNext(at(11, 19, 19, 0), 21, 30))
    }

    @Test
    fun `a time already gone fires tomorrow`() {
        assertEquals(Duration.ofHours(23), delayUntilNext(at(11, 19, 22, 30), 21, 30))
    }

    @Test
    fun `exactly now waits a full day`() {
        assertEquals(Duration.ofDays(1), delayUntilNext(at(11, 19, 21, 30), 21, 30))
    }

    @Test
    fun `the night the clocks go back the wait is an hour longer`() {
        // Spain moves from CEST to CET in the early hours of 25 October 2026.
        assertEquals(Duration.ofHours(25), delayUntilNext(at(10, 24, 21, 30), 21, 30))
    }
}
