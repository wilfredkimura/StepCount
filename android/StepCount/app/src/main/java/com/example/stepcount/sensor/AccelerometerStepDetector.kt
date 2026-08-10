package com.example.stepcount.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt

/**
 * Fallback motion detector using the device's 3-axis Accelerometer sensor.
 * Detects rhythmic peaks in magnitude when the hardware step counter is absent.
 */
class AccelerometerStepDetector(
    private val onStepDetected: () -> Unit
) : SensorEventListener {

    private var lastMagnitude = 0f
    private var isPeak = false

    // Sensitivity threshold for walking peak detection
    private val stepThreshold = 11.8f
    private val resetThreshold = 9.8f
    private var lastStepTimestamp = 0L
    private val minStepIntervalMs = 250L // Prevents counting faster than humanly possible (max 4 steps/sec)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate 3-dimensional vector magnitude of movement
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val now = System.currentTimeMillis()

        if (magnitude > stepThreshold && !isPeak && (now - lastStepTimestamp > minStepIntervalMs)) {
            isPeak = true
            lastStepTimestamp = now
            onStepDetected()
        } else if (magnitude < resetThreshold) {
            isPeak = false
        }

        lastMagnitude = magnitude
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed for accuracy change
    }
}
