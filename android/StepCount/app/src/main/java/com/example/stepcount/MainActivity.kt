package com.example.stepcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.stepcount.core.theme.StepCountTheme
import com.example.stepcount.presentation.navigation.AppNavGraph
import com.example.stepcount.worker.StepSyncWorker

/**
 * Main Activity hosting the navigation graph, theme, and background sync worker initialization.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start 24/7 background step tracking service & midnight rollover alarm
        com.example.stepcount.sensor.StepForegroundService.startService(applicationContext)
        com.example.stepcount.sensor.MidnightStepRolloverReceiver.scheduleMidnightAlarm(applicationContext)

        // Enqueue periodic background step checkpoints and sync
        com.example.stepcount.worker.StepPeriodicCheckWorker.enqueuePeriodicCheck(applicationContext)
        StepSyncWorker.enqueuePeriodicSync(applicationContext)

        setContent {
            StepCountTheme {
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