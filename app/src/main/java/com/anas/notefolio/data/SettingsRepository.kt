package com.anas.notefolio.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "notefolio_settings")

object SettingsKeys {
    val THEME = stringPreferencesKey("theme")           // "system" | "light" | "dark"
    val LANGUAGE = stringPreferencesKey("language")      // "en" | "ur" (Roman Urdu)
    val ACCENT = stringPreferencesKey("accent")          // "blue" | "green" | "orange" | "purple"
    val ICON = stringPreferencesKey("icon")              // "default" | "alt1" | "alt2" | "alt3"
}

class SettingsRepository(private val context: Context) {
    val theme: Flow<String> = context.dataStore.data.map { it[SettingsKeys.THEME] ?: "system" }
    val language: Flow<String> = context.dataStore.data.map { it[SettingsKeys.LANGUAGE] ?: "en" }
    val accent: Flow<String> = context.dataStore.data.map { it[SettingsKeys.ACCENT] ?: "blue" }
    val icon: Flow<String> = context.dataStore.data.map { it[SettingsKeys.ICON] ?: "default" }

    suspend fun setTheme(value: String) = context.dataStore.edit { it[SettingsKeys.THEME] = value }
    suspend fun setLanguage(value: String) = context.dataStore.edit { it[SettingsKeys.LANGUAGE] = value }
    suspend fun setAccent(value: String) = context.dataStore.edit { it[SettingsKeys.ACCENT] = value }
    suspend fun setIcon(value: String) = context.dataStore.edit { it[SettingsKeys.ICON] = value }
}
