package com.anas.notefolio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.notefolio.data.NoteFolioBackup
import com.anas.notefolio.data.NoteRepository
import com.anas.notefolio.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val theme: String = "system",
    val language: String = "en",
    val accent: String = "blue",
    val icon: String = "default"
)

sealed class ImportExportResult {
    object Idle : ImportExportResult()
    object ExportSuccess : ImportExportResult()
    object ImportSuccess : ImportExportResult()
    object ImportFailed : ImportExportResult()
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    val uiState = combine(
        settingsRepository.theme, settingsRepository.language,
        settingsRepository.accent, settingsRepository.icon
    ) { theme, language, accent, icon ->
        SettingsUiState(theme, language, accent, icon)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(v: String) = viewModelScope.launch { settingsRepository.setTheme(v) }
    fun setLanguage(v: String) = viewModelScope.launch { settingsRepository.setLanguage(v) }
    fun setAccent(v: String) = viewModelScope.launch { settingsRepository.setAccent(v) }
    fun setIcon(v: String) = viewModelScope.launch { settingsRepository.setIcon(v) }

    suspend fun exportJson(): String {
        val (notes, folders) = noteRepository.exportAll()
        return NoteFolioBackup.export(notes, folders)
    }

    suspend fun importJson(jsonText: String, replaceExisting: Boolean): Boolean {
        val parsed = NoteFolioBackup.import(jsonText) ?: return false
        noteRepository.importAll(parsed.first, parsed.second, replaceExisting)
        return true
    }
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val noteRepository: NoteRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(settingsRepository, noteRepository) as T
    }
}
