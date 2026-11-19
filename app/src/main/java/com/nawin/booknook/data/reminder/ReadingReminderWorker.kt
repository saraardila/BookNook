package com.nawin.booknook.data.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nawin.booknook.R

/** Shows the daily "time to read" notification; tapping it opens the app. */
class ReadingReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        showReminder(applicationContext)
        return Result.success()
    }
}

private const val CHANNEL_ID = "reading_reminder"
private const val NOTIFICATION_ID = 1
private const val OPEN_APP_REQUEST = 0

private fun showReminder(context: Context) {
    val manager = NotificationManagerCompat.from(context)
    // The reader may have revoked the permission or muted the app since turning it on.
    val permitted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    if (!permitted || !manager.areNotificationsEnabled()) return

    manager.createNotificationChannel(
        NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.reminder_channel_name))
            .build()
    )
    val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
        PendingIntent.getActivity(context, OPEN_APP_REQUEST, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_reminder)
        .setContentTitle(context.getString(R.string.reminder_notification_title))
        .setContentText(context.getString(R.string.reminder_notification_text))
        .setContentIntent(openApp)
        .setAutoCancel(true)
        .build()
    manager.notify(NOTIFICATION_ID, notification)
}
