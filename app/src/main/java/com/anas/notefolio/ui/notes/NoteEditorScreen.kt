package com.anas.notefolio.ui.notes

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.anas.notefolio.data.local.FolderEntity
import com.anas.notefolio.ui.theme.*

private val editorSwatches = listOf(
    "default" to SpineDefault, "red" to SpineRed, "orange" to SpineOrange,
    "yellow" to SpineYellow, "green" to SpineGreen, "teal" to SpineTeal,
    "blue" to SpineBlue, "purple" to SpinePurple, "pink" to SpinePink
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    folders: List<FolderEntity>,
    onBack: () -> Unit,
    onAddFolder: (String) -> Unit
) {
    val note by viewModel.note.collectAsState()
    var colorMenuOpen by remember { mutableStateOf(false) }
    var folderMenuOpen by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }
    var checklistInput by remember { mutableStateOf("") }
    var newFolderName by remember { mutableStateOf("") }
    var addFolderDialog by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var showDrawing by remember { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                val current = note?.body.orEmpty()
                viewModel.updateBody(if (current.isBlank()) spoken else "$current $spoken")
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val tts = remember { com.anas.notefolio.util.TtsManager(context) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }

    val n = note ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { viewModel.saveNow(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note...")
                        }
                        speechLauncher.launch(intent)
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice to text")
                    }
                    IconButton(onClick = { showDrawing = true }) {
                        Icon(Icons.Default.Brush, contentDescription = "Sketch")
                    }
                    IconButton(onClick = { showQr = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Share as QR")
                    }
                    IconButton(onClick = {
                        if (isSpeaking) {
                            tts.stop(); isSpeaking = false
                        } else {
                            val text = if (n.isChecklist) n.checklist.joinToString(". ") { it.text } else n.body
                            tts.speak("${n.title}. $text")
                            isSpeaking = true
                        }
                    }) {
                        Icon(
                            if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Read aloud"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleChecklistMode() }) {
                        Icon(Icons.Default.CheckBox, contentDescription = "Checklist mode",
                            tint = if (n.isChecklist) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(if (n.pinned) Icons.Default.PushPin else Icons.Outlined.PushPin, contentDescription = "Pin")
                    }
                    Box {
                        IconButton(onClick = { colorMenuOpen = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Color")
                        }
                        DropdownMenu(expanded = colorMenuOpen, onDismissRequest = { colorMenuOpen = false }) {
                            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                editorSwatches.forEach { (key, color) ->
                                    Box(
                                        Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (n.colorKey == key) 3.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                viewModel.setColor(key)
                                                colorMenuOpen = false
                                            }
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.toggleArchived() }) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive",
                            tint = if (n.archived) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                    IconButton(onClick = {
                        val cal = java.util.Calendar.getInstance()
                        android.app.DatePickerDialog(context, { _, y, m, d ->
                            cal.set(y, m, d)
                            android.app.TimePickerDialog(context, { _, h, min ->
                                cal.set(java.util.Calendar.HOUR_OF_DAY, h)
                                cal.set(java.util.Calendar.MINUTE, min)
                                viewModel.setReminder(cal.timeInMillis)
                            }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false).show()
                        }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show()
                    }) {
                        Icon(Icons.Default.Alarm, contentDescription = "Set reminder",
                            tint = if (n.reminderAt != null) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                    IconButton(onClick = { viewModel.moveToTrash(onBack) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = n.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    placeholder = { Text("Title") },
                    textStyle = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Folder selector
            item {
                Box {
                    AssistChip(
                        onClick = { folderMenuOpen = true },
                        label = { Text(folders.find { it.id == n.folderId }?.name ?: "No folder") },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
                    )
                    DropdownMenu(expanded = folderMenuOpen, onDismissRequest = { folderMenuOpen = false }) {
                        DropdownMenuItem(text = { Text("No folder") }, onClick = {
                            viewModel.setFolder(null); folderMenuOpen = false
                        })
                        folders.forEach { f ->
                            DropdownMenuItem(text = { Text(f.name) }, onClick = {
                                viewModel.setFolder(f.id); folderMenuOpen = false
                            })
                        }
                        DropdownMenuItem(text = { Text("+ New folder") }, onClick = {
                            folderMenuOpen = false; addFolderDialog = true
                        })
                    }
                }
            }

            // Tags row
            item {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        n.tags.forEach { tag ->
                            AssistChip(
                                onClick = { viewModel.removeTag(tag) },
                                label = { Text("#$tag") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        placeholder = { Text("Add a tag and press enter") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.addTag(tagInput); tagInput = ""
                        }),
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    )
                }
            }

            // Attached sketch preview, if any
            n.logoDataUrl?.let { dataUrl ->
                item {
                    val bmp = remember(dataUrl) { decodeBase64Bitmap(dataUrl) }
                    if (bmp != null) {
                        Box {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Attached sketch",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            )
                            IconButton(
                                onClick = { viewModel.removeSketch() },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove sketch")
                            }
                        }
                    }
                }
            }

            if (n.isChecklist) {
                items(n.checklist, key = { it.id }) { item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = item.done, onCheckedChange = { viewModel.toggleChecklistItem(item.id) })
                        OutlinedTextField(
                            value = item.text,
                            onValueChange = { viewModel.editChecklistItem(item.id, it) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = { viewModel.removeChecklistItem(item.id) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove item")
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = checklistInput,
                        onValueChange = { checklistInput = it },
                        placeholder = { Text("Add checklist item and press enter") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.addChecklistItem(checklistInput); checklistInput = ""
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                item {
                    OutlinedTextField(
                        value = n.body,
                        onValueChange = { viewModel.updateBody(it) },
                        placeholder = { Text("Start writing...") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp)
                    )
                }
            }

            item {
                Text(
                    "${n.body.split("\\s+".toRegex()).filter { it.isNotBlank() }.size} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (addFolderDialog) {
        AlertDialog(
            onDismissRequest = { addFolderDialog = false },
            title = { Text("New folder") },
            text = {
                OutlinedTextField(value = newFolderName, onValueChange = { newFolderName = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newFolderName.isNotBlank()) onAddFolder(newFolderName.trim())
                    newFolderName = ""; addFolderDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { addFolderDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDrawing) {
        DrawingCanvasDialog(
            onDismiss = { showDrawing = false },
            onSave = { dataUrl -> viewModel.setSketch(dataUrl) }
        )
    }

    if (showQr) {
        val (payload, truncated) = com.anas.notefolio.util.QrShare.buildPayload(n.title, n.body)
        val qrBitmap = remember(payload) { com.anas.notefolio.util.QrShare.generate(payload) }
        AlertDialog(
            onDismissRequest = { showQr = false },
            title = { Text("Share via QR") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR code",
                        modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp)
                    )
                    if (truncated) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Note is long — QR contains the first ${com.anas.notefolio.util.QrShare.MAX_CHARS} characters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showQr = false }) { Text("Done") } }
        )
    }
}

private fun decodeBase64Bitmap(dataUrl: String): android.graphics.Bitmap? {
    return try {
        val base64 = dataUrl.substringAfter(",", dataUrl)
        val bytes = android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}
