package com.anas.notefolio.data

import com.anas.notefolio.data.local.FolderEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Backup format:
 * {
 *   "app": "NoteFolio",
 *   "version": 1,
 *   "exportedAt": <epoch ms>,
 *   "folders": [ {id, name, sortOrder}, ... ],
 *   "notes": [ {id, title, body, folderId, tags, colorKey, checklist, isChecklist,
 *               pinned, archived, deletedAt, reminderAt, createdAt, updatedAt}, ... ]
 * }
 */
object NoteFolioBackup {

    fun export(notes: List<Note>, folders: List<FolderEntity>): String {
        val root = JSONObject()
        root.put("app", "NoteFolio")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val foldersArr = JSONArray()
        folders.forEach { f ->
            val o = JSONObject()
            o.put("id", f.id); o.put("name", f.name); o.put("sortOrder", f.sortOrder)
            foldersArr.put(o)
        }
        root.put("folders", foldersArr)

        val notesArr = JSONArray()
        notes.forEach { n ->
            val o = JSONObject()
            o.put("id", n.id)
            o.put("title", n.title)
            o.put("body", n.body)
            o.put("folderId", n.folderId ?: JSONObject.NULL)
            o.put("tags", JSONArray(n.tags))
            o.put("colorKey", n.colorKey)
            val checklistArr = JSONArray()
            n.checklist.forEach { item ->
                val io = JSONObject()
                io.put("id", item.id); io.put("text", item.text); io.put("done", item.done)
                checklistArr.put(io)
            }
            o.put("checklist", checklistArr)
            o.put("isChecklist", n.isChecklist)
            o.put("pinned", n.pinned)
            o.put("archived", n.archived)
            o.put("deletedAt", n.deletedAt ?: JSONObject.NULL)
            o.put("reminderAt", n.reminderAt ?: JSONObject.NULL)
            o.put("createdAt", n.createdAt)
            o.put("updatedAt", n.updatedAt)
            notesArr.put(o)
        }
        root.put("notes", notesArr)

        return root.toString(2)
    }

    /** Returns null if the JSON isn't a recognizable NoteFolio (or legacy Writify) backup. */
    fun import(jsonText: String): Pair<List<Note>, List<FolderEntity>>? {
        return try {
            val root = JSONObject(jsonText)
            val foldersArr = root.optJSONArray("folders") ?: JSONArray()
            val folders = (0 until foldersArr.length()).map { i ->
                val o = foldersArr.getJSONObject(i)
                FolderEntity(o.getString("id"), o.getString("name"), o.optInt("sortOrder", 0))
            }

            val notesArr = root.optJSONArray("notes") ?: JSONArray()
            val notes = (0 until notesArr.length()).map { i ->
                val o = notesArr.getJSONObject(i)
                val tagsArr = o.optJSONArray("tags") ?: JSONArray()
                val tags = (0 until tagsArr.length()).map { tagsArr.getString(it) }
                val checklistArr = o.optJSONArray("checklist") ?: JSONArray()
                val checklist = (0 until checklistArr.length()).map { j ->
                    val io = checklistArr.getJSONObject(j)
                    ChecklistItem(io.getString("id"), io.getString("text"), io.getBoolean("done"))
                }
                Note(
                    id = o.getString("id"),
                    title = o.optString("title", ""),
                    body = o.optString("body", ""),
                    folderId = if (o.isNull("folderId")) null else o.optString("folderId"),
                    tags = tags,
                    colorKey = o.optString("colorKey", "default"),
                    checklist = checklist,
                    isChecklist = o.optBoolean("isChecklist", false),
                    pinned = o.optBoolean("pinned", false),
                    archived = o.optBoolean("archived", false),
                    deletedAt = if (o.isNull("deletedAt")) null else o.optLong("deletedAt"),
                    reminderAt = if (o.isNull("reminderAt")) null else o.optLong("reminderAt"),
                    logoDataUrl = null,
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                )
            }
            Pair(notes, folders)
        } catch (e: Exception) {
            null
        }
    }
}
