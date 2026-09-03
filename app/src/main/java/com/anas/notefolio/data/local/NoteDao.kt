package com.anas.notefolio.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // Active notes: not archived, not in trash
    @Query("SELECT * FROM notes WHERE archived = 0 AND deletedAt IS NULL ORDER BY pinned DESC, updatedAt DESC")
    fun getActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE archived = 1 AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NoteEntity?

    @Query("""
        SELECT * FROM notes
        WHERE archived = 0 AND deletedAt IS NULL
        AND (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')
        ORDER BY pinned DESC, updatedAt DESC
    """)
    fun search(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("UPDATE notes SET deletedAt = :ts WHERE id = :id")
    suspend fun moveToTrash(id: String, ts: Long)

    @Query("UPDATE notes SET deletedAt = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: String)

    @Query("UPDATE notes SET archived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("UPDATE notes SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("UPDATE notes SET colorKey = :colorKey WHERE id = :id")
    suspend fun setColor(id: String, colorKey: String)

    // Auto-purge: permanently delete anything sitting in Trash longer than the retention window
    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun purgeTrashOlderThan(cutoff: Long)

    @Query("SELECT * FROM notes")
    suspend fun getAllForExport(): List<NoteEntity>

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}
