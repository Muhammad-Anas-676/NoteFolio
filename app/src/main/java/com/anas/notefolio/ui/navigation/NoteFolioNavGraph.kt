package com.anas.notefolio.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anas.notefolio.data.NoteRepository
import com.anas.notefolio.data.SecurityRepository
import com.anas.notefolio.data.SettingsRepository
import com.anas.notefolio.data.local.FolderEntity
import com.anas.notefolio.ui.lock.LockScreen
import com.anas.notefolio.ui.lock.LockViewModel
import com.anas.notefolio.ui.lock.LockViewModelFactory
import com.anas.notefolio.ui.notes.*
import com.anas.notefolio.ui.settings.SettingsScreen
import com.anas.notefolio.ui.settings.SettingsViewModel
import com.anas.notefolio.ui.settings.SettingsViewModelFactory
import kotlinx.coroutines.launch
import java.util.UUID

object Routes {
    const val NOTES_LIST = "notes_list"
    const val NOTE_EDITOR = "note_editor/{noteId}"
    fun noteEditor(id: String) = "note_editor/$id"
    const val SETTINGS = "settings"
    const val ARCHIVE = "archive"
    const val TRASH = "trash"
    const val STATS = "stats"
    const val PIN_SETUP = "pin_setup"
}

@Composable
fun NoteFolioNavGraph(
    repository: NoteRepository,
    settingsRepository: SettingsRepository,
    securityRepository: SecurityRepository
) {
    val navController: NavHostController = rememberNavController()
    val notesViewModel: NotesViewModel = viewModel(factory = NotesViewModelFactory(repository))
    val folders by notesViewModel.folders.collectAsState()
    val scope = rememberCoroutineScopeCompat()

    NavHost(navController = navController, startDestination = Routes.NOTES_LIST) {
        composable(Routes.NOTES_LIST) {
            NotesListScreen(
                viewModel = notesViewModel,
                onOpenNote = { id -> navController.navigate(Routes.noteEditor(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenArchive = { navController.navigate(Routes.ARCHIVE) },
                onOpenTrash = { navController.navigate(Routes.TRASH) },
                onOpenStats = { navController.navigate(Routes.STATS) }
            )
        }
        composable(
            route = Routes.NOTE_EDITOR,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            val editorViewModel: NoteEditorViewModel = viewModel(
                key = noteId,
                factory = NoteEditorViewModelFactory(repository, noteId, androidx.compose.ui.platform.LocalContext.current.applicationContext)
            )
            NoteEditorScreen(
                viewModel = editorViewModel,
                folders = folders,
                onBack = { navController.popBackStack() },
                onAddFolder = { name ->
                    scope.launch {
                        val folder = FolderEntity(id = UUID.randomUUID().toString(), name = name)
                        repository.upsertFolder(folder)
                        editorViewModel.setFolder(folder.id)
                    }
                }
            )
        }
        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(settingsRepository, repository)
            )
            val lockViewModel: LockViewModel = viewModel(factory = LockViewModelFactory(securityRepository))
            SettingsScreen(
                viewModel = settingsViewModel,
                lockViewModel = lockViewModel,
                onBack = { navController.popBackStack() },
                onSetupPin = { navController.navigate(Routes.PIN_SETUP) }
            )
        }
        composable(Routes.PIN_SETUP) {
            val lockViewModel: LockViewModel = viewModel(factory = LockViewModelFactory(securityRepository))
            LockScreen(
                mode = "setup",
                biometricAvailable = false,
                biometricEnabled = false,
                errorTick = 0,
                onVerify = { _, _ -> },
                onSetupComplete = { pin ->
                    lockViewModel.setPin(pin)
                    navController.popBackStack()
                },
                onBiometricRequested = { }
            )
        }
        composable(Routes.ARCHIVE) {
            val archiveViewModel: ArchiveViewModel = viewModel(factory = ArchiveViewModelFactory(repository))
            ArchiveScreen(
                viewModel = archiveViewModel,
                onBack = { navController.popBackStack() },
                onOpenNote = { id -> navController.navigate(Routes.noteEditor(id)) }
            )
        }
        composable(Routes.TRASH) {
            val trashViewModel: TrashViewModel = viewModel(factory = TrashViewModelFactory(repository))
            TrashScreen(viewModel = trashViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.STATS) {
            val statsViewModel: com.anas.notefolio.ui.stats.StatsViewModel = viewModel(
                factory = com.anas.notefolio.ui.stats.StatsViewModelFactory(repository)
            )
            com.anas.notefolio.ui.stats.StatsScreen(viewModel = statsViewModel, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun rememberCoroutineScopeCompat() = androidx.compose.runtime.rememberCoroutineScope()
