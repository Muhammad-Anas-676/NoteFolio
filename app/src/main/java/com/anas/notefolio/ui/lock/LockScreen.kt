package com.anas.notefolio.ui.lock

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private const val PIN_LENGTH = 4

/**
 * mode = "verify" -> unlocking an existing PIN
 * mode = "setup"  -> first-time PIN creation (asks twice)
 */
@Composable
fun LockScreen(
    mode: String,
    biometricAvailable: Boolean,
    biometricEnabled: Boolean,
    errorTick: Int,
    onVerify: (String, (Boolean) -> Unit) -> Unit,
    onSetupComplete: (String) -> Unit,
    onBiometricRequested: () -> Unit
) {
    var entered by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf<String?>(null) } // used during setup's confirm step
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(errorTick) {
        if (errorTick > 0) {
            errorMsg = "Incorrect PIN"
            entered = ""
            shake.snapTo(0f)
            shake.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(300))
            shake.snapTo(0f)
        }
    }

    fun onDigit(d: String) {
        if (entered.length >= PIN_LENGTH) return
        entered += d
        if (entered.length == PIN_LENGTH) {
            when (mode) {
                "verify" -> onVerify(entered) { ok -> if (!ok) { /* handled via errorTick */ } }
                "setup" -> {
                    if (firstPin == null) {
                        firstPin = entered
                        entered = ""
                        errorMsg = null
                    } else if (firstPin == entered) {
                        onSetupComplete(entered)
                    } else {
                        errorMsg = "PINs didn't match — try again"
                        firstPin = null
                        entered = ""
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier
                .graphicsLayer { translationX = (Math.sin(shake.value * Math.PI * 4).toFloat()) * 10f }
        ) {
            Text(
                when {
                    mode == "setup" && firstPin == null -> "Set a PIN"
                    mode == "setup" -> "Confirm your PIN"
                    else -> "Enter PIN"
                },
                style = MaterialTheme.typography.titleLarge
            )

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(PIN_LENGTH) { i ->
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < entered.length) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            val rows = listOf(
                listOf("1", "2", "3"), listOf("4", "5", "6"),
                listOf("7", "8", "9"), listOf("bio", "0", "back")
            )
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    row.forEach { key ->
                        when (key) {
                            "back" -> KeyButton(icon = Icons.Default.Backspace) {
                                if (entered.isNotEmpty()) entered = entered.dropLast(1)
                            }
                            "bio" -> if (mode == "verify" && biometricAvailable && biometricEnabled) {
                                KeyButton(icon = Icons.Default.Fingerprint) { onBiometricRequested() }
                            } else {
                                Spacer(Modifier.size(64.dp))
                            }
                            else -> KeyButton(label = key) { onDigit(key) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(label: String? = null, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    Box(
        Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.titleLarge)
        } else if (icon != null) {
            Icon(icon, contentDescription = null)
        }
    }
}
