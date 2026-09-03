package com.anas.notefolio.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.notefolio.data.NoteRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val activeCount: Int = 0,
    val archivedCount: Int = 0,
    val trashedCount: Int = 0,
    val pinnedCount: Int = 0,
    val checklistCount: Int = 0,
    val totalWords: Int = 0,
    val topTags: List<Pair<String, Int>> = emptyList()
)

class StatsViewModel(repository: NoteRepository) : ViewModel() {
    val uiState = combine(
        repository.activeNotes(), repository.archivedNotes(), repository.trashedNotes()
    ) { active, archived, trashed ->
        val totalWords = active.sumOf { n -> n.body.split("\\s+".toRegex()).count { it.isNotBlank() } }
        val tagCounts = active.flatMap { it.tags }.groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }

        StatsUiState(
            activeCount = active.size,
            archivedCount = archived.size,
            trashedCount = trashed.size,
            pinnedCount = active.count { it.pinned },
            checklistCount = active.count { it.isChecklist },
            totalWords = totalWords,
            topTags = tagCounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())
}

class StatsViewModelFactory(private val repository: NoteRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = StatsViewModel(repository) as T
}
