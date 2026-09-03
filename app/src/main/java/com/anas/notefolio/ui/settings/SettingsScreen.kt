package com.anas.notefolio.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anas.notefolio.ui.strings.t
import com.anas.notefolio.ui.theme.*
import com.anas.notefolio.util.IconManager
import kotlinx.coroutines.launch

private val accentSwatches = listOf(
    "blue" to LightAccent, "green" to SpineGreen, "orange" to LightHighlight, "purple" to SpinePurple
)
private val iconSwatches = listOf(
    "default" to LightAccent, "alt1" to LightHighlight, "alt2" to SpineGreen, "alt3" to SpinePurple
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    lockViewModel: com.anas.notefolio.ui.lock.LockViewModel,
    onBack: () -> Unit,
    onSetupPin: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val lockEnabled by lockViewModel.lockEnabled.collectAsState()
    val biometricEnabled by lockViewModel.biometricEnabled.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var importReplaceDialog by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val json = viewModel.exportJson()
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            snackbarHostState.showSnackbar(t("exportSuccess", state.language))
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importReplaceDialog = uri
    }

    LaunchedEffect(state.icon) {
        IconManager.applyIcon(context, state.icon)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(t("settings", state.language)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSection(t("theme", state.language)) {
                SingleChoiceSegment(
                    options = listOf(
                        "system" to t("themeSystem", state.language),
                        "light" to t("themeLight", state.language),
                        "dark" to t("themeDark", state.language)
                    ),
                    selected = state.theme,
                    onSelect = { viewModel.setTheme(it) }
                )
            }

            SettingsSection(t("accentColor", state.language)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    accentSwatches.forEach { (key, color) ->
                        SwatchDot(color, selected = state.accent == key) { viewModel.setAccent(key) }
                    }
                }
            }

            SettingsSection(t("language", state.language)) {
                SingleChoiceSegment(
                    options = listOf("en" to "English", "ur" to "Roman Urdu"),
                    selected = state.language,
                    onSelect = { viewModel.setLanguage(it) }
                )
            }

            SettingsSection(t("appIcon", state.language)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    iconSwatches.forEach { (key, color) ->
                        SwatchDot(color, selected = state.icon == key) { viewModel.setIcon(key) }
                    }
                }
            }

            HorizontalDivider()

            SettingsSection("App Lock") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(if (lockEnabled) "PIN lock is on" else "PIN lock is off")
                        Switch(
                            checked = lockEnabled,
                            onCheckedChange = { enabled -> if (enabled) onSetupPin() else lockViewModel.disableLock() }
                        )
                    }
                    if (lockEnabled) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Unlock with fingerprint/face")
                            Switch(checked = biometricEnabled, onCheckedChange = { lockViewModel.setBiometricEnabled(it) })
                        }
                    }
                }
            }

            HorizontalDivider()

            SettingsSection(t("secData", state.language)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { exportLauncher.launch("notefolio-backup.json") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(t("exportData", state.language)) }

                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(t("importData", state.language)) }
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                t("madeBy", state.language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    importReplaceDialog?.let { uri ->
        AlertDialog(
            onDismissRequest = { importReplaceDialog = null },
            title = { Text(t("importData", state.language)) },
            text = { Text("Merge with existing notes, or replace everything with the backup?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        val ok = text != null && viewModel.importJson(text, replaceExisting = false)
                        snackbarHostState.showSnackbar(
                            if (ok) t("importSuccess", state.language) else t("importFailed", state.language)
                        )
                        importReplaceDialog = null
                    }
                }) { Text("Merge") }
            },
            dismissButton = {
                TextButton(onClick = {
                    scope.launch {
                        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        val ok = text != null && viewModel.importJson(text, replaceExisting = true)
                        snackbarHostState.showSnackbar(
                            if (ok) t("importSuccess", state.language) else t("importFailed", state.language)
                        )
                        importReplaceDialog = null
                    }
                }) { Text("Replace all") }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun SwatchDot(color: androidx.compose.ui.graphics.Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(width = if (selected) 3.dp else 0.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun SingleChoiceSegment(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (key, label) ->
            FilterChip(selected = selected == key, onClick = { onSelect(key) }, label = { Text(label) })
        }
    }
}
