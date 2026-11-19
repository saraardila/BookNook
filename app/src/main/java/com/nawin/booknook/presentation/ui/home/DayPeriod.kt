package com.nawin.booknook.presentation.ui.home

import androidx.annotation.StringRes
import com.nawin.booknook.R

/** Part of the day used to greet the reader on Home. */
enum class DayPeriod(@param:StringRes val greetingRes: Int) {
    Morning(R.string.greeting_morning),
    Afternoon(R.string.greeting_afternoon),
    Evening(R.string.greeting_evening),
    Night(R.string.greeting_night);

    companion object {
        /** Maps an hour of the day (0–23) to its period. */
        fun fromHour(hour: Int): DayPeriod = when (hour) {
            in 5..11 -> Morning
            in 12..17 -> Afternoon
            in 18..21 -> Evening
            else -> Night
        }
    }
}
