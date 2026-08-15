package com.example.stepcount.worker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.work.*
import com.example.stepcount.core.di.AppContainer
import com.example.stepcount.widget.TodayStepWidgetReceiver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

/**
 * Background worker that samples the hardware step counter periodically (~every 15 to 60 mins).
 * Computes step deltas, saves them to Room database, and refreshes the Home Screen Widget.
 *
 * Why this is battery-efficient:
 * It attaches to the low-power hardware step sensor for under 500ms, commits the reading,
 * and immediately unregisters to release CPU execution.
 */
class StepPeriodicCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val container = AppContainer(applicationContext)
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (sensorManager != null && stepSensor != null) {
            val stepReadingDeferred = CompletableDeferred<Long?>()

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                        val rawSteps = event.values[0].toLong()
                        stepReadingDeferred.complete(rawSteps)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // No action needed
                }
            }

            // Register listener temporarily with short UI delay for rapid register read
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)

            try {
                // Wait up to 1000ms for hardware register sample
                val rawHardwareSteps = withTimeoutOrNull(1000L) {
                    stepReadingDeferred.await()
                }

                if (rawHardwareSteps != null) {
                    // Process reading and update Room database & Widget
                    container.stepDeltaTracker.processHardwareReading(rawHardwareSteps)
                    android.util.Log.i("StepPeriodicCheckWorker", "Background step sample processed: $rawHardwareSteps")
                }
            } catch (e: Exception) {
                android.util.Log.e("StepPeriodicCheckWorker", "Error sampling background steps", e)
            } finally {
                sensorManager.unregisterListener(listener)
            }
        }

        // Refresh the home screen widget
        TodayStepWidgetReceiver.notifyStepsUpdated(applicationContext)

        // Sync unsynced steps to backend if network is available
        try {
            container.syncPendingStepsUseCase()
        } catch (e: Exception) {
            // Ignore backend sync failure in periodic worker
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "StepPeriodicCheckWorker"

        /**
         * Enqueues periodic step checking and widget refresh.
         * Runs every 15 minutes with a 5-minute flex window.
         */
        fun enqueuePeriodicCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<StepPeriodicCheckWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
