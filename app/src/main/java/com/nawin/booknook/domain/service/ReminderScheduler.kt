package com.nawin.booknook.domain.service

/**
 * Schedules the daily reading reminder. Each platform implements it with its
 * own background scheduler; the rest of the app only sees this interface.
 */
interface ReminderScheduler {
    /** Shows the reminder every day at [hour]:[minute], replacing any previous schedule. */
    fun schedule(hour: Int, minute: Int)

    /** Stops the reminder. */
    fun cancel()
}
