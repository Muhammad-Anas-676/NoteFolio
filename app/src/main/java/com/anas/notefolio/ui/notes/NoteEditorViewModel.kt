package com.anas.notefolio.ui.notes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.notefolio.data.ChecklistItem
import com.anas.notefolio.data.Note
import com.anas.notefolio.data.NoteRepository
import com.anas.notefolio.util.BatteryAwareness
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class NoteEditorViewModel(
    private val repository: NoteRepository,
    private val noteId: String,
    private val appContext: Context
) : ViewModel() {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private var autosaveJob: Job? = null

    init {
        viewModelScope.launch {
            _note.value = repository.getNote(noteId)
        }
    }

    private fun mutate(transform: (Note) -> Note) {
        val current = _note.value ?: return
        val updated = transform(current)
        _note.value = updated
        scheduleAutosave()
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            // Battery Saver on -> wait longer before writing to disk, fewer wakeups.
            delay(BatteryAwareness.autosaveDebounceMs(appContext))
            val current = _note.value ?: return@launch
            _saving.value = true
            repository.saveNote(current)
            _saving.value = false
        }
    }

    fun saveNow() = viewModelScope.launch {
        autosaveJob?.cancel()
        _note.value?.let { repository.saveNote(it) }
    }

    fun updateTitle(v: String) = mutate { it.copy(title = v) }
    fun updateBody(v: String) = mutate { it.copy(body = v) }
    fun setFolder(folderId: String?) = mutate { it.copy(folderId = folderId) }
    fun setColor(colorKey: String) = mutate { it.copy(colorKey = colorKey) }
    fun setSketch(dataUrl: String) = mutate { it.copy(logoDataUrl = dataUrl) }
    fun removeSketch() = mutate { it.copy(logoDataUrl = null) }
    fun setReminder(triggerAtMillis: Long) = mutate { note ->
        com.anas.notefolio.util.ReminderScheduler.schedule(appContext, note.id, triggerAtMillis)
        note.copy(reminderAt = triggerAtMillis)
    }
    fun togglePin() = mutate { it.copy(pinned = !it.pinned) }
    fun toggleArchived() = mutate { it.copy(archived = !it.archived) }
    fun toggleChecklistMode() = mutate { it.copy(isChecklist = !it.isChecklist) }

    fun addTag(tag: String) = mutate {
        val clean = tag.trim().removePrefix("#")
        if (clean.isBlank() || it.tags.contains(clean)) it else it.copy(tags = it.tags + clean)
    }
    fun removeTag(tag: String) = mutate { it.copy(tags = it.tags - tag) }

    fun addChecklistItem(text: String) = mutate {
        if (text.isBlank()) it
        else it.copy(checklist = it.checklist + ChecklistItem(UUID.randomUUID().toString(), text.trim(), false))
    }
    fun toggleChecklistItem(id: String) = mutate { note ->
        note.copy(checklist = note.checklist.map { if (it.id == id) it.copy(done = !it.done) else it })
    }
    fun removeChecklistItem(id: String) = mutate { note ->
        note.copy(checklist = note.checklist.filterNot { it.id == id })
    }
    fun editChecklistItem(id: String, text: String) = mutate { note ->
        note.copy(checklist = note.checklist.map { if (it.id == id) it.copy(text = text) else it })
    }

    fun moveToTrash(onDone: () -> Unit) = viewModelScope.launch {
        autosaveJob?.cancel()
        repository.moveToTrash(noteId)
        onDone()
    }

    override fun onCleared() {
        super.onCleared()
        // Best-effort final flush; explicit saveNow() on back-press is the primary path.
        if (autosaveJob?.isActive == true) {
            _note.value?.let { n ->
                viewModelScope.launch { repository.saveNote(n) }
            }
        }
    }
}

class NoteEditorViewModelFactory(
    private val repository: NoteRepository,
    private val noteId: String,
    private val appContext: Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteEditorViewModel(repository, noteId, appContext) as T
    }
}
