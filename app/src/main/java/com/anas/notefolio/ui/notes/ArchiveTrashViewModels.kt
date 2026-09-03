package com.anas.notefolio.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.notefolio.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchiveViewModel(private val repository: NoteRepository) : ViewModel() {
    val notes = repository.archivedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unarchive(id: String) = viewModelScope.launch { repository.setArchived(id, false) }
    fun moveToTrash(id: String) = viewModelScope.launch { repository.moveToTrash(id) }
}

class TrashViewModel(private val repository: NoteRepository) : ViewModel() {
    val notes = repository.trashedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restore(id: String) = viewModelScope.launch { repository.restoreFromTrash(id) }
    fun deleteForever(id: String) = viewModelScope.launch { repository.deletePermanently(id) }
}

class ArchiveViewModelFactory(private val repository: NoteRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ArchiveViewModel(repository) as T
}

class TrashViewModelFactory(private val repository: NoteRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = TrashViewModel(repository) as T
}
