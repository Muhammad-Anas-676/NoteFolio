package com.anas.notefolio.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private fun uniqueWorkName(noteId: String) = "notefolio_reminder_$noteId"

    /** Schedules (or replaces) a one-time reminder for [noteId] to fire at [triggerAtMillis]. */
    fun schedule(context: Context, noteId: String, triggerAtMillis: Long) {
        val delay = (triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val data = Data.Builder().putString(ReminderWorker.KEY_NOTE_ID, noteId).build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName(noteId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, noteId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(noteId))
    }
}
