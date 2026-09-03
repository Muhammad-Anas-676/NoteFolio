package com.anas.notefolio.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_ID = "notefolio_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID, "Note reminders", NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders you set on individual notes"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun show(context: Context, notificationId: Int, title: String, body: String) {
        ensureChannel(context)
        // Caller is responsible for having checked POST_NOTIFICATIONS permission on API 33+.
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // replace with @drawable/ic_launcher_foreground in Android Studio if desired
            .setContentTitle(title.ifBlank { "NoteFolio reminder" })
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).apply {
            try {
                notify(notificationId, notification)
            } catch (e: SecurityException) {
                // POST_NOTIFICATIONS not granted (Android 13+) — silently skip rather than crash.
                // The reminder was still processed; only the visible alert is missing.
            }
        }
    }
}
