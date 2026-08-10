package com.example.stepcount.domain.model

import com.example.stepcount.core.util.Constants
import java.util.Locale

/**
 * Pure domain model for a single day's step record.
 * Automatically computes calories burned, walking distance, active minutes, and goal percentage.
 */
data class DailyStepRecord(
    val id: Long = 0,
    val userId: String,
    val date: String, // Format: yyyy-MM-dd
    val steps: Long,
    val goal: Int = Constants.DEFAULT_DAILY_GOAL,
    val isSynced: Boolean = false
) {
    /**
     * Estimated calories burned in kilocalories (kcal).
     */
    val caloriesBurned: Int
        get() = (steps * Constants.CALORIES_PER_STEP).toInt()

    /**
     * Estimated walking distance in kilometers.
     */
    val distanceKm: Double
        get() = (steps * Constants.AVERAGE_STRIDE_METERS) / 1000.0

    /**
     * Formatted distance string (e.g. "6.2 km").
     */
    val formattedDistance: String
        get() = String.format(Locale.US, "%.1f km", distanceKm)

    /**
     * Estimated active duration in minutes.
     */
    val activeMinutes: Int
        get() = (steps / Constants.STEPS_PER_ACTIVE_MINUTE).toInt()

    /**
     * Goal achievement progress percentage (e.g. 84.3%).
     */
    val goalPercentage: Float
        get() = if (goal > 0) (steps.toFloat() / goal.toFloat()) * 100f else 0f

    /**
     * Returns true if the user achieved or exceeded their daily step goal.
     */
    val isGoalMet: Boolean
        get() = steps >= goal
}
