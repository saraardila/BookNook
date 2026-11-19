package com.nawin.booknook.presentation.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class DayPeriodTest {

    @Test
    fun `hours map to the expected period at every boundary`() {
        val expected = mapOf(
            0 to DayPeriod.Night,
            4 to DayPeriod.Night,
            5 to DayPeriod.Morning,
            11 to DayPeriod.Morning,
            12 to DayPeriod.Afternoon,
            17 to DayPeriod.Afternoon,
            18 to DayPeriod.Evening,
            21 to DayPeriod.Evening,
            22 to DayPeriod.Night,
            23 to DayPeriod.Night,
        )
        expected.forEach { (hour, period) -> assertEquals("hour $hour", period, DayPeriod.fromHour(hour)) }
    }
}
