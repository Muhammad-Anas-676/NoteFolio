package com.anas.notefolio.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

/**
 * Hashes the PIN using an HMAC key that lives inside the Android Keystore
 * (hardware-backed on most devices) instead of storing the PIN — or even a
 * plain SHA-256 of it — in DataStore. The key never leaves the secure
 * element/TEE, so the resulting hash can't be replayed on another device
 * even if the DataStore file itself is extracted.
 */
object PinCrypto {
    private const val KEY_ALIAS = "notefolio_pin_hmac_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun hashPin(pin: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(getOrCreateKey())
        val bytes = mac.doFinal(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, storedHash: String): Boolean = hashPin(pin) == storedHash
}
