package com.anas.notefolio.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anas.notefolio.NoteFolioApp

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val noteId = inputData.getString(KEY_NOTE_ID) ?: return Result.failure()
        val repository = (applicationContext as NoteFolioApp).repository
        val note = repository.getNote(noteId) ?: return Result.success() // note deleted, nothing to remind about

        NotificationHelper.show(
            context = applicationContext,
            notificationId = noteId.hashCode(),
            title = note.title,
            body = note.body.take(120)
        )
        return Result.success()
    }

    companion object {
        const val KEY_NOTE_ID = "note_id"
    }
}
