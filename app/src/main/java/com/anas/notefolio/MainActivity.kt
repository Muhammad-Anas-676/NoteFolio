package com.anas.notefolio

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anas.notefolio.ui.lock.LockScreen
import com.anas.notefolio.ui.lock.LockViewModel
import com.anas.notefolio.ui.lock.LockViewModelFactory
import com.anas.notefolio.ui.navigation.NoteFolioNavGraph
import com.anas.notefolio.ui.theme.NoteFolioTheme
import com.anas.notefolio.util.BiometricHelper

// FragmentActivity (not plain ComponentActivity) is required by androidx.biometric's BiometricPrompt.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as NoteFolioApp
        val repository = app.repository
        val settingsRepository = app.settingsRepository
        val securityRepository = app.securityRepository

        setContent {
            val theme by settingsRepository.theme.collectAsState(initial = "system")
            val accent by settingsRepository.accent.collectAsState(initial = "blue")

            NoteFolioTheme(themePreference = theme, accentKey = accent) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val lockViewModel: LockViewModel = viewModel(factory = LockViewModelFactory(securityRepository))
                    val lockEnabled by lockViewModel.lockEnabled.collectAsState()
                    val biometricEnabled by lockViewModel.biometricEnabled.collectAsState()
                    val unlocked by lockViewModel.unlocked.collectAsState()
                    val errorTick by lockViewModel.errorTick.collectAsState()
                    val biometricAvailable = BiometricHelper.isAvailable(this@MainActivity)

                    if (lockEnabled && !unlocked) {
                        LockScreen(
                            mode = "verify",
                            biometricAvailable = biometricAvailable,
                            biometricEnabled = biometricEnabled,
                            errorTick = errorTick,
                            onVerify = { pin, cb -> lockViewModel.tryUnlock(pin, cb) },
                            onSetupComplete = { /* not used in verify mode */ },
                            onBiometricRequested = {
                                BiometricHelper.prompt(
                                    this@MainActivity,
                                    onSuccess = { lockViewModel.unlockViaBiometric() }
                                )
                            }
                        )
                    } else {
                        NoteFolioNavGraph(
                            repository = repository,
                            settingsRepository = settingsRepository,
                            securityRepository = securityRepository
                        )
                    }
                }
            }
        }
    }
}
