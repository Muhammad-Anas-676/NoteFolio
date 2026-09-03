package com.anas.notefolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single note row. Lives in the app's private, per-device SQLite
 * database at /data/data/com.anas.notefolio/databases/notefolio.db —
 * i.e. exactly the "local storage in a folder on the user's own phone"
 * behavior Anas asked for. Nothing here ever leaves the device unless
 * the user explicitly exports it.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val body: String = "",
    val folderId: String? = null,
    val tags: String = "",           // comma-separated tag list
    val colorKey: String = "default", // maps to Spine* colors in Color.kt
    val checklistJson: String? = null, // JSON array of {id,text,done} when note is a checklist
    val isChecklist: Boolean = false,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val deletedAt: Long? = null,     // null = active, non-null = in Trash since this time
    val reminderAt: Long? = null,
    val logoDataUrl: String? = null, // optional attached sketch/image (base64), used from Batch 3
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
