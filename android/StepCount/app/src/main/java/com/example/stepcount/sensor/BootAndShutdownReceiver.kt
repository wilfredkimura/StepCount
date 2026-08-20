package com.example.stepcount.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.stepcount.core.di.AppContainer
import com.example.stepcount.widget.TodayStepWidgetReceiver
import com.example.stepcount.worker.StepPeriodicCheckWorker
import com.example.stepcount.worker.StepSyncWorker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * BroadcastReceiver preserving step accuracy across device power cycles.
 *
 * Why this is needed:
 * 1. ACTION_SHUTDOWN: Takes an instant sensor snapshot before power cuts off,
 *    saving all steps walked right up to the shutdown moment.
 * 2. ACTION_BOOT_COMPLETED: Resets the hardware baseline to 0 and re-enqueues
 *    background periodic workers so tracking resumes smoothly.
 */
class BootAndShutdownReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = AppContainer(context.applicationContext)

                when (action) {
                    Intent.ACTION_SHUTDOWN, "android.intent.action.QUICKBOOT_POWEROFF" -> {
                        android.util.Log.i("BootAndShutdownReceiver", "Shutdown detected. Taking final step snapshot...")
                        sampleAndCommitSteps(context, container)
                    }

                    Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                        android.util.Log.i("BootAndShutdownReceiver", "Boot / package replacement detected. Resetting baseline and restarting background tracking...")
                        // Reset baseline for new hardware lifecycle
                        container.stepDeltaTracker.onDeviceRebooted()

                        // Start 24/7 background foreground tracking service
                        StepForegroundService.startService(context.applicationContext)

                        // Schedule exact midnight step rollover alarm
                        MidnightStepRolloverReceiver.scheduleMidnightAlarm(context.applicationContext)

                        // Re-enqueue background workers
                        StepPeriodicCheckWorker.enqueuePeriodicCheck(context.applicationContext)
                        StepSyncWorker.enqueuePeriodicSync(context.applicationContext)

                        // Refresh home screen widget
                        TodayStepWidgetReceiver.notifyStepsUpdated(context.applicationContext)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BootAndShutdownReceiver", "Error processing broadcast action: $action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun sampleAndCommitSteps(context: Context, container: AppContainer) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return

        val deferredReading = CompletableDeferred<Long?>()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    deferredReading.complete(event.values[0].toLong())
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
        try {
            val rawHardwareSteps = withTimeoutOrNull(800L) {
                deferredReading.await()
            }
            if (rawHardwareSteps != null) {
                container.stepDeltaTracker.processHardwareReading(rawHardwareSteps)
                android.util.Log.i("BootAndShutdownReceiver", "Final pre-shutdown steps saved: $rawHardwareSteps")
            }
        } finally {
            sensorManager.unregisterListener(listener)
        }
    }
}
