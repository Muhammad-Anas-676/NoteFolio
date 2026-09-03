package com.anas.notefolio.data

import com.anas.notefolio.data.local.NoteEntity
import org.json.JSONArray
import org.json.JSONObject

data class ChecklistItem(val id: String, val text: String, val done: Boolean)

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val folderId: String?,
    val tags: List<String>,
    val colorKey: String,
    val checklist: List<ChecklistItem>,
    val isChecklist: Boolean,
    val pinned: Boolean,
    val archived: Boolean,
    val deletedAt: Long?,
    val reminderAt: Long?,
    val logoDataUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    body = body,
    folderId = folderId,
    tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    colorKey = colorKey,
    checklist = parseChecklist(checklistJson),
    isChecklist = isChecklist,
    pinned = pinned,
    archived = archived,
    deletedAt = deletedAt,
    reminderAt = reminderAt,
    logoDataUrl = logoDataUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    body = body,
    folderId = folderId,
    tags = tags.joinToString(","),
    colorKey = colorKey,
    checklistJson = serializeChecklist(checklist),
    isChecklist = isChecklist,
    pinned = pinned,
    archived = archived,
    deletedAt = deletedAt,
    reminderAt = reminderAt,
    logoDataUrl = logoDataUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun parseChecklist(json: String?): List<ChecklistItem> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ChecklistItem(o.getString("id"), o.getString("text"), o.getBoolean("done"))
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun serializeChecklist(items: List<ChecklistItem>): String? {
    if (items.isEmpty()) return null
    val arr = JSONArray()
    items.forEach {
        val o = JSONObject()
        o.put("id", it.id); o.put("text", it.text); o.put("done", it.done)
        arr.put(o)
    }
    return arr.toString()
}
