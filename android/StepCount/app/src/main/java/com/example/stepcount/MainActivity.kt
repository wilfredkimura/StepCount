package com.example.stepcount

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.stepcount.core.theme.AppThemeMode
import com.example.stepcount.core.theme.StepCountTheme
import com.example.stepcount.presentation.navigation.AppNavGraph
import com.example.stepcount.worker.StepSyncWorker

/**
 * Main Activity hosting the navigation graph, dynamic theme updates, and background sync worker initialization.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start 24/7 background step tracking service & midnight rollover alarm
        com.example.stepcount.sensor.StepForegroundService.startService(applicationContext)
        com.example.stepcount.sensor.MidnightStepRolloverReceiver.scheduleMidnightAlarm(applicationContext)

        // Enqueue periodic background step checkpoints and auto sync
        com.example.stepcount.worker.StepPeriodicCheckWorker.enqueuePeriodicCheck(applicationContext)
        StepSyncWorker.enqueuePeriodicSync(applicationContext)

        val prefs = getSharedPreferences(com.example.stepcount.core.util.Constants.PREFS_NAME, MODE_PRIVATE)

        setContent {
            val themeModeState = remember {
                val initialKey = prefs.getString(com.example.stepcount.core.util.Constants.KEY_THEME_MODE, AppThemeMode.SYSTEM.storageKey)
                mutableStateOf(AppThemeMode.fromStorageKey(initialKey))
            }

            DisposableEffect(prefs) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == com.example.stepcount.core.util.Constants.KEY_THEME_MODE || key == com.example.stepcount.core.util.Constants.KEY_DARK_MODE) {
                        val newKey = prefs.getString(com.example.stepcount.core.util.Constants.KEY_THEME_MODE, AppThemeMode.SYSTEM.storageKey)
                        themeModeState.value = AppThemeMode.fromStorageKey(newKey)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            StepCountTheme(themeMode = themeModeState.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}