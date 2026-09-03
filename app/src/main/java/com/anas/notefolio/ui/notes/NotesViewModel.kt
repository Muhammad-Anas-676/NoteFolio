package com.anas.notefolio.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.notefolio.data.Note
import com.anas.notefolio.data.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortMode { UPDATED_DESC, CREATED_DESC, TITLE_ASC }

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _sortMode = MutableStateFlow(SortMode.UPDATED_DESC)
    val sortMode: StateFlow<SortMode> = _sortMode

    private val _selectedFolder = MutableStateFlow<String?>(null) // null = "All"
    val selectedFolder: StateFlow<String?> = _selectedFolder

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView

    val folders = repository.folders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val rawNotes: Flow<List<Note>> = _query.flatMapLatest { q ->
        if (q.isBlank()) repository.activeNotes() else repository.search(q)
    }

    val notes: StateFlow<List<Note>> = combine(rawNotes, _selectedFolder, _sortMode) { list, folder, sort ->
        val filtered = if (folder == null) list else list.filter { it.folderId == folder }
        when (sort) {
            SortMode.UPDATED_DESC -> filtered.sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.updatedAt })
            SortMode.CREATED_DESC -> filtered.sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.createdAt })
            SortMode.TITLE_ASC -> filtered.sortedWith(compareByDescending<Note> { it.pinned }.thenBy { it.title.lowercase() })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _query.value = q }
    fun setSortMode(mode: SortMode) { _sortMode.value = mode }
    fun setFolder(folderId: String?) { _selectedFolder.value = folderId }
    fun toggleView() { _isGridView.value = !_isGridView.value }

    fun togglePin(note: Note) = viewModelScope.launch { repository.setPinned(note.id, !note.pinned) }
    fun archive(note: Note) = viewModelScope.launch { repository.setArchived(note.id, true) }
    fun moveToTrash(note: Note) = viewModelScope.launch { repository.moveToTrash(note.id) }
    fun setColor(note: Note, colorKey: String) = viewModelScope.launch { repository.setColor(note.id, colorKey) }

    fun createNote(onCreated: (String) -> Unit) = viewModelScope.launch {
        val note = repository.createNote()
        onCreated(note.id)
    }

    init {
        // mirrors purgeOldTrash() run at startup in the original app
        viewModelScope.launch { repository.purgeOldTrash() }
    }
}

class NotesViewModelFactory(private val repository: NoteRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotesViewModel(repository) as T
    }
}
