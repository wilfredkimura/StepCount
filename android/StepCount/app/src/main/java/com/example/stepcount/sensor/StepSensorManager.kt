package com.example.stepcount.sensor

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.stepcount.core.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages device hardware step sensors and fallback accelerometer motion detection.
 * Calculates live daily steps by subtracting the midnight baseline count from hardware totals.
 */
class StepSensorManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val onStepCountUpdated: (Long) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _liveSteps = MutableStateFlow(0L)
    val liveSteps: StateFlow<Long> = _liveSteps.asStateFlow()

    private val _isFallbackActive = MutableStateFlow(false)
    val isFallbackActive: StateFlow<Boolean> = _isFallbackActive.asStateFlow()

    private var accelerometerDetector: AccelerometerStepDetector? = null
    private var isListening = false

    private fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }

    /**
     * Starts listening to step sensors in a lifecycle-aware manner.
     */
    fun startListening(initialTodaySteps: Long = 0L) {
        if (isListening) return
        isListening = true

        _liveSteps.value = initialTodaySteps
        android.util.Log.d("StepSensorManager", "startListening: Hardware TYPE_STEP_COUNTER present=${stepCounterSensor != null}, Accelerometer present=${accelerometerSensor != null}")

        if (stepCounterSensor != null) {
            _isFallbackActive.value = false
            val registered = sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            android.util.Log.i("StepSensorManager", "Registered TYPE_STEP_COUNTER sensor (success=$registered)")
        } else if (accelerometerSensor != null) {
            // Automatic fallback to 3-axis accelerometer peak detector
            _isFallbackActive.value = true
            accelerometerDetector = AccelerometerStepDetector {
                val newCount = _liveSteps.value + 1
                _liveSteps.value = newCount
                onStepCountUpdated(newCount)
                android.util.Log.d("StepSensorManager", "Accelerometer step detected! Count: $newCount")
            }
            val registered = sensorManager.registerListener(accelerometerDetector, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
            android.util.Log.i("StepSensorManager", "Registered Accelerometer fallback sensor (success=$registered)")
        }
    }

    /**
     * Unregisters sensor listeners to preserve device battery life when screen/app is inactive.
     */
    fun stopListening() {
        if (!isListening) return
        isListening = false
        android.util.Log.d("StepSensorManager", "stopListening: Unregistering sensor listeners.")
        sensorManager.unregisterListener(this)
        accelerometerDetector?.let {
            sensorManager.unregisterListener(it)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        val totalHardwareSteps = event.values[0].toLong()
        val today = getTodayDateString()
        val savedDate = prefs.getString(Constants.KEY_LAST_RECORDED_DATE, null)

        var baseline = prefs.getLong(Constants.KEY_DAILY_BASELINE, -1L)

        // Midnight Reset Logic: If new day has started or baseline is uninitialized
        if (savedDate != today || baseline < 0L || totalHardwareSteps < baseline) {
            baseline = totalHardwareSteps
            prefs.edit()
                .putString(Constants.KEY_LAST_RECORDED_DATE, today)
                .putLong(Constants.KEY_DAILY_BASELINE, baseline)
                .apply()
            android.util.Log.i("StepSensorManager", "New daily baseline initialized: $baseline for date $today")
        }

        val calculatedTodaySteps = (totalHardwareSteps - baseline).coerceAtLeast(0L)
        _liveSteps.value = calculatedTodaySteps
        android.util.Log.d("StepSensorManager", "onSensorChanged: raw=$totalHardwareSteps, baseline=$baseline, calculatedToday=$calculatedTodaySteps")

        scope.launch {
            onStepCountUpdated(calculatedTodaySteps)
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed for accuracy change
    }
}
