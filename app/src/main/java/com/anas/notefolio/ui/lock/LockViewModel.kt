package com.anas.notefolio.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.notefolio.data.SecurityRepository
import com.anas.notefolio.util.PinCrypto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LockViewModel(private val securityRepository: SecurityRepository) : ViewModel() {

    val lockEnabled: StateFlow<Boolean> = securityRepository.lockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val biometricEnabled: StateFlow<Boolean> = securityRepository.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked

    private val _errorTick = MutableStateFlow(0)
    val errorTick: StateFlow<Int> = _errorTick // bump this to trigger a shake animation on wrong PIN

    fun tryUnlock(pin: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val hash = securityRepository.pinHash.first()
        val ok = hash != null && PinCrypto.verifyPin(pin, hash)
        if (ok) _unlocked.value = true else _errorTick.value += 1
        onResult(ok)
    }

    fun setPin(pin: String) = viewModelScope.launch {
        securityRepository.setPinHash(PinCrypto.hashPin(pin))
        _unlocked.value = true
    }

    fun disableLock() = viewModelScope.launch { securityRepository.clearPin() }
    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch { securityRepository.setBiometricEnabled(enabled) }
    fun unlockViaBiometric() { _unlocked.value = true }
    fun lock() { _unlocked.value = false }
}

class LockViewModelFactory(private val securityRepository: SecurityRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = LockViewModel(securityRepository) as T
}
