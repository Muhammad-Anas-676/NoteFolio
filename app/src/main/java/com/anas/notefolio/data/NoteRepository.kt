package com.anas.notefolio.data

import com.anas.notefolio.data.local.FolderEntity
import com.anas.notefolio.data.local.NoteDao
import com.anas.notefolio.data.local.FolderDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Trash auto-purge window — mirrors purgeOldTrash() from the original app:
 * anything sitting in Trash longer than this gets permanently deleted.
 */
private val TRASH_RETENTION_MS = TimeUnit.DAYS.toMillis(30)

class NoteRepository(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) {
    fun activeNotes(): Flow<List<Note>> = noteDao.getActiveNotes().map { list -> list.map { it.toDomain() } }
    fun archivedNotes(): Flow<List<Note>> = noteDao.getArchivedNotes().map { list -> list.map { it.toDomain() } }
    fun trashedNotes(): Flow<List<Note>> = noteDao.getTrashedNotes().map { list -> list.map { it.toDomain() } }
    fun search(query: String): Flow<List<Note>> = noteDao.search(query).map { list -> list.map { it.toDomain() } }
    fun folders(): Flow<List<FolderEntity>> = folderDao.getAll()

    suspend fun getNote(id: String): Note? = noteDao.getById(id)?.toDomain()

    suspend fun createNote(): Note {
        val note = Note(
            id = UUID.randomUUID().toString(),
            title = "", body = "", folderId = null, tags = emptyList(),
            colorKey = "default", checklist = emptyList(), isChecklist = false,
            pinned = false, archived = false, deletedAt = null, reminderAt = null,
            logoDataUrl = null,
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
        )
        noteDao.upsert(note.toEntity())
        return note
    }

    suspend fun saveNote(note: Note) {
        noteDao.upsert(note.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    suspend fun moveToTrash(id: String) = noteDao.moveToTrash(id, System.currentTimeMillis())
    suspend fun restoreFromTrash(id: String) = noteDao.restoreFromTrash(id)
    suspend fun deletePermanently(id: String) = noteDao.deletePermanently(id)
    suspend fun setArchived(id: String, archived: Boolean) = noteDao.setArchived(id, archived)
    suspend fun setPinned(id: String, pinned: Boolean) = noteDao.setPinned(id, pinned)
    suspend fun setColor(id: String, colorKey: String) = noteDao.setColor(id, colorKey)

    suspend fun purgeOldTrash() {
        noteDao.purgeTrashOlderThan(System.currentTimeMillis() - TRASH_RETENTION_MS)
    }

    suspend fun upsertFolder(folder: FolderEntity) = folderDao.upsert(folder)
    suspend fun deleteFolder(id: String) = folderDao.delete(id)

    suspend fun exportAll(): Pair<List<Note>, List<FolderEntity>> {
        return Pair(noteDao.getAllForExport().map { it.toDomain() }, folderDao.getAllForExport())
    }

    suspend fun importAll(notes: List<Note>, folders: List<FolderEntity>, replaceExisting: Boolean) {
        if (replaceExisting) {
            noteDao.clearAll()
            folderDao.clearAll()
        }
        folders.forEach { folderDao.upsert(it) }
        notes.forEach { noteDao.upsert(it.toEntity()) }
    }
}
