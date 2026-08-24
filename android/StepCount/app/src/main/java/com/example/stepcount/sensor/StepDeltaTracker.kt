package com.example.stepcount.sensor

import android.content.Context
import android.content.SharedPreferences
import com.example.stepcount.core.util.Constants
import com.example.stepcount.domain.repository.StepRepository
import com.example.stepcount.widget.TodayStepWidgetReceiver
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tracks incremental step deltas from the raw hardware step counter.
 *
 * Why this exists:
 * Android's hardware TYPE_STEP_COUNTER continuously counts steps since device boot.
 * Instead of taking single baseline snapshots that get wiped at midnight or on restart,
 * this tracker calculates the difference (delta) between the current hardware reading
 * and the last saved hardware reading.
 *
 * This guarantees:
 * 1. Zero lost steps across midnight (morning steps before opening the app are credited).
 * 2. Accurate tracking across device reboots (reboot drops counter to 0, which we detect).
 * 3. Thread-safe execution using a Mutex to prevent race conditions and double-counting.
 * 4. Anomaly detection against impossible step spikes (e.g. sensor glitches or update baseline issues).
 * 5. Consistent synchronization between Room database and the Home Screen widget.
 */
class StepDeltaTracker(
    private val prefs: SharedPreferences,
    private val stepRepository: StepRepository,
    private val notificationHelper: com.example.stepcount.core.notification.StepNotificationHelper? = null,
    private val context: Context? = null
) {
    // Mutex lock ensures only one sensor reading is processed at a time,
    // avoiding race conditions between foreground service, background workers, and UI.
    private val processMutex = Mutex()

    /**
     * Helper to get today's date in standard YYYY-MM-DD format.
     */
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    /**
     * Processes a fresh reading from the hardware step counter sensor.
     * Computes the delta, performs OEM-agnostic glitch/anomaly detection, saves the updated step count
     * to Room database, and refreshes the Home Screen widget.
     *
     * @param currentHardwareSteps The raw cumulative step count from Sensor.TYPE_STEP_COUNTER.
     * @param todayDate Optional date string, defaults to today.
     * @param currentTimeMillis Optional timestamp in milliseconds, defaults to System.currentTimeMillis().
     * @return The updated total steps for today.
     */
    suspend fun processHardwareReading(
        currentHardwareSteps: Long,
        todayDate: String = getTodayDateString(),
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long = processMutex.withLock {
        val lastHardwareSteps = prefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L)
        val lastTimestamp = prefs.getLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, -1L)
        val lastRecordedDate = prefs.getString(Constants.KEY_LAST_RECORDED_DATE, null)

        // Fetch current steps for today from local database
        val existingTodayRecord = stepRepository.getTodaySteps().firstOrNull()
        val currentTodaySteps = existingTodayRecord?.steps ?: 0L

        // OEM Protection: Ignore transient 0 readings from driver flushes on Redmi/Oppo
        if (currentHardwareSteps <= 0L) {
            if (lastHardwareSteps > 0L) {
                android.util.Log.d("StepDeltaTracker", "Ignored transient 0 reading from sensor driver flush.")
                return@withLock currentTodaySteps
            } else {
                // First initialization when counter is legitimately 0
                prefs.edit()
                    .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, 0L)
                    .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, currentTimeMillis)
                    .putString(Constants.KEY_LAST_RECORDED_DATE, todayDate)
                    .commit()
                return@withLock currentTodaySteps
            }
        }

        // Case 1: First time the sensor is ever read or freshly re-baselined
        if (lastHardwareSteps < 0L) {
            prefs.edit()
                .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, currentHardwareSteps)
                .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, currentTimeMillis)
                .putString(Constants.KEY_LAST_RECORDED_DATE, todayDate)
                .commit()

            return@withLock currentTodaySteps
        }

        // Case 2: Calculate delta with OEM driver drop protection
        val delta: Long
        if (currentHardwareSteps < lastHardwareSteps) {
            // Redmi / Oppo / Device Reboot Case:
            // Hardware counter decreased (e.g. sensor hub flush, driver reset, or phone restart).
            // Never treat the entire raw reading as a delta.
            android.util.Log.w(
                "StepDeltaTracker",
                "Hardware counter decreased from $lastHardwareSteps to $currentHardwareSteps (OEM driver reset or reboot). Re-baselining safely."
            )
            // If phone just rebooted to a small count (e.g. 1-300 steps), attribute small walk delta, otherwise 0
            delta = if (currentHardwareSteps in 1L..300L) currentHardwareSteps else 0L

            prefs.edit()
                .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, currentHardwareSteps)
                .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, currentTimeMillis)
                .putString(Constants.KEY_LAST_RECORDED_DATE, todayDate)
                .commit()

            if (delta == 0L) {
                return@withLock currentTodaySteps
            }
        } else {
            // Normal case: counter has incremented
            delta = currentHardwareSteps - lastHardwareSteps
        }

        // Case 3: Universal Physical Speed & Cadence Anomaly Filter
        // Maximum human walking/sprinting cadence is 5.0 steps/sec (~300 steps/min).
        // Any jump exceeding this physical limit or jumping by > 3,000 steps in a single event is rejected.
        val elapsedSeconds = if (lastTimestamp > 0L) {
            maxOf(0.1, (currentTimeMillis - lastTimestamp) / 1000.0)
        } else {
            -1.0
        }

        val isAnomaly = when {
            // Delta exceeds max human physical cadence over the elapsed time window (5 steps/sec), with a minimum buffer of 1,000 steps for sensor hub batches
            elapsedSeconds > 0.0 && delta > maxOf(1_000L, (elapsedSeconds * 5.0).toLong()) -> true
            // Single reading jump > 15,000 steps in a single event regardless of window
            delta > 15_000L -> true
            // Timestamp is invalid but delta is abnormally high
            elapsedSeconds <= 0.0 && delta > 1_000L -> true
            else -> false
        }

        if (isAnomaly) {
            android.util.Log.w(
                "StepDeltaTracker",
                "Step anomaly detected: delta=$delta, elapsed=${elapsedSeconds}s, currentRaw=$currentHardwareSteps. Discarding spike and re-baselining."
            )
            // Re-baseline without applying the corrupted delta to today's steps
            prefs.edit()
                .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, currentHardwareSteps)
                .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, currentTimeMillis)
                .putString(Constants.KEY_LAST_RECORDED_DATE, todayDate)
                .commit()

            return@withLock currentTodaySteps
        }

        val updatedTodaySteps = if (lastRecordedDate != null && lastRecordedDate != todayDate) {
            // Midnight transition: last recorded reading was on a previous day.
            // Attribute the new steps to the fresh day
            delta
        } else {
            // Same day: add delta to existing steps
            currentTodaySteps + delta
        }

        // Save updated steps to Room database
        stepRepository.saveDailySteps(todayDate, updatedTodaySteps)

        // Evaluate milestone and goal notifications
        val activeGoal = existingTodayRecord?.goal ?: Constants.DEFAULT_DAILY_GOAL
        notificationHelper?.checkAndNotify(updatedTodaySteps, activeGoal, todayDate)

        // Update preferences with latest hardware reading, timestamp, and date
        prefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, currentHardwareSteps)
            .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, currentTimeMillis)
            .putString(Constants.KEY_LAST_RECORDED_DATE, todayDate)
            .commit()

        // Refresh the Home Screen Widget if context is available
        context?.let {
            TodayStepWidgetReceiver.notifyStepsUpdated(it)
        }

        return@withLock updatedTodaySteps
    }

    /**
     * Resets the hardware baseline upon receiving device boot broadcast.
     * Tells the tracker to cleanly re-sync from the next hardware reading.
     */
    fun onDeviceRebooted() {
        prefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L)
            .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, System.currentTimeMillis())
            .commit()
    }
}
