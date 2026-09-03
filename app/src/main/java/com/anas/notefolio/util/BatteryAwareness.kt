package com.anas.notefolio.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Reacts to the phone's real battery-saver toggle (Settings > Battery > Battery Saver),
 * not just charge level. When it's on, NoteFolio backs off anything that costs CPU/radio:
 *  - autosave debounce grows from 500ms to 2000ms (fewer disk writes)
 *  - reminder background checks batch less aggressively (see ReminderScheduler)
 *  - TTS/animations are unaffected (Anas asked specifically for saved battery on background work)
 */
@Composable
fun rememberIsPowerSaveMode(): Boolean {
    val context = LocalContext.current
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    var isPowerSave by remember { mutableStateOf(powerManager.isPowerSaveMode) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                isPowerSave = powerManager.isPowerSaveMode
            }
        }
        context.registerReceiver(receiver, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    return isPowerSave
}

object BatteryAwareness {
    const val AUTOSAVE_DEBOUNCE_NORMAL_MS = 500L
    const val AUTOSAVE_DEBOUNCE_SAVER_MS = 2000L

    fun isPowerSaveMode(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isPowerSaveMode
    }

    fun autosaveDebounceMs(context: Context): Long =
        if (isPowerSaveMode(context)) AUTOSAVE_DEBOUNCE_SAVER_MS else AUTOSAVE_DEBOUNCE_NORMAL_MS
}
