package com.example.stepcount.core.util

/**
 * App-wide constants for default configurations, database names, and sensor defaults.
 * These simple values are used across the app for consistent behavior.
 */
object Constants {
    // Database Name
    const val DATABASE_NAME = "stepcount_db"

    // Default Daily Step Goal
    const val DEFAULT_DAILY_GOAL = 8000

    // Average stride length in meters (used to calculate distance from steps)
    const val AVERAGE_STRIDE_METERS = 0.762

    // Average calories burned per step for standard walking pace
    const val CALORIES_PER_STEP = 0.04

    // Average steps per minute for moderate walking pace
    const val STEPS_PER_ACTIVE_MINUTE = 100

    // Shared Preferences Keys
    const val PREFS_NAME = "stepcount_preferences"
    const val KEY_DAILY_BASELINE = "key_daily_baseline"
    const val KEY_LAST_HARDWARE_COUNTER = "key_last_hardware_counter"
    const val KEY_LAST_RECORDED_DATE = "key_last_recorded_date"
    const val KEY_DARK_MODE = "key_dark_mode"
    const val KEY_STEP_UNITS = "key_step_units"
    const val KEY_GUEST_MODE = "key_guest_mode"

    // Backend Base URL (delegates to AppConfig for easy local vs Render switching)
    val BACKEND_BASE_URL: String
        get() = AppConfig.BASE_URL
}
