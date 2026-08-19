package com.example.stepcount.sensor

import android.content.Context
import android.content.SharedPreferences
import com.example.stepcount.core.util.Constants
import com.example.stepcount.domain.repository.StepRepository
import com.example.stepcount.widget.TodayStepWidgetReceiver
import kotlinx.coroutines.flow.firstOrNull
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
 * 3. Consistent synchronization between Room database and the Home Screen widget.
 */
class StepDeltaTracker(
    private val prefs: SharedPreferences,
    private val stepRepository: StepRepository,
    private val notificationHelper: com.example.stepcount.core.notification.StepNotificationHelper? = null,
    private val context: Context? = null
) {

    /**
     * Helper to get today's date in standard YYYY-MM-DD format.
     */
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    /**
     * Processes a fresh reading from the hardware step counter sensor.
     * Computes the delta, saves the updated step count to Room database,
     * and refreshes the Home Screen widget.
     *
     * @param currentHardwareSteps The raw cumulative step count from Sensor.TYPE_STEP_COUNTER.
     * @param todayDate Optional date string, defaults to today.
     * @return The updated total steps for today.
     */
    suspend fun processHardwareReading(
        currentHardwareSteps: Long,
        todayDate: String = getTodayDateString()
    ): Long {
        val lastHardwareSteps = prefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L)
        val lastRecordedDate = prefs.getString(Constants.KEY_LAST_RECORDED_DATE, null)

        // Case 1: First time the sensor is ever read
        if (lastHardwareSteps < 0L) {
            prefs.edit()
                .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, currentHardwareSteps)
                .putString(Constants.KEY_LAST_RECORDED_DATE, todayDate)
                .apply()

            val existingRecord = stepRepository.getTodaySteps().firstOrNull()
            return existingRecord?.steps ?: 0L
        }

        // Case 2: Calculate the delta since our last sensor reading
        val delta = if (currentHardwareSteps >= lastHardwareSteps) {
            // Normal case: counter has incremented
            currentHardwareSteps - lastHardwareSteps
        } else {
            // Reboot case: device was restarted and counter reset to 0
            // currentHardwareSteps represents all steps taken since the reboot
            currentHardwareSteps
        }

        // Fetch current steps for today from local database
        val existingTodayRecord = stepRepository.getTodaySteps().firstOrNull()
        val currentTodaySteps = existingTodayRecord?.steps ?: 0L

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

        // Update preferences with latest hardware reading and date
        prefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, currentHardwareSteps)
            .putString(Constants.KEY_LAST_RECORDED_DATE, todayDate)
            .apply()

        // Refresh the Home Screen Widget if context is available
        context?.let {
            TodayStepWidgetReceiver.notifyStepsUpdated(it)
        }

        return updatedTodaySteps
    }

    /**
     * Resets the hardware baseline upon receiving device boot broadcast.
     * Tells the tracker that the hardware counter will now start from 0.
     */
    fun onDeviceRebooted() {
        prefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, 0L)
            .apply()
    }
}
