package com.nawin.booknook.data.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nawin.booknook.domain.service.ReminderScheduler
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Daily reminder on WorkManager: it survives reboots and needs no exact-alarm
 * permission, at the cost of arriving a few minutes late when the system
 * batches work to save battery.
 */
class WorkManagerReminderScheduler(private val context: Context) : ReminderScheduler {

    override fun schedule(hour: Int, minute: Int) {
        val delay = delayUntilNext(ZonedDateTime.now(), hour, minute)
        val request = PeriodicWorkRequestBuilder<ReadingReminderWorker>(REPEAT_DAYS, TimeUnit.DAYS)
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        // Cancel and re-enqueue so a new time also resets the first run.
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "reading_reminder"
        const val REPEAT_DAYS = 1L
    }
}
