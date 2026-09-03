package com.anas.notefolio.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.securityDataStore by preferencesDataStore(name = "notefolio_security")

private object SecurityKeys {
    val PIN_HASH = stringPreferencesKey("pin_hash")       // HMAC-SHA256(PIN), Keystore-backed key — never the raw PIN
    val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    val LOCK_ENABLED = booleanPreferencesKey("lock_enabled")
}

class SecurityRepository(private val context: Context) {
    val pinHash: Flow<String?> = context.securityDataStore.data.map { it[SecurityKeys.PIN_HASH] }
    val biometricEnabled: Flow<Boolean> = context.securityDataStore.data.map { it[SecurityKeys.BIOMETRIC_ENABLED] ?: false }
    val lockEnabled: Flow<Boolean> = context.securityDataStore.data.map { it[SecurityKeys.LOCK_ENABLED] ?: false }

    suspend fun setPinHash(hash: String) = context.securityDataStore.edit {
        it[SecurityKeys.PIN_HASH] = hash
        it[SecurityKeys.LOCK_ENABLED] = true
    }

    suspend fun clearPin() = context.securityDataStore.edit {
        it.remove(SecurityKeys.PIN_HASH)
        it[SecurityKeys.LOCK_ENABLED] = false
        it[SecurityKeys.BIOMETRIC_ENABLED] = false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) = context.securityDataStore.edit {
        it[SecurityKeys.BIOMETRIC_ENABLED] = enabled
    }
}
