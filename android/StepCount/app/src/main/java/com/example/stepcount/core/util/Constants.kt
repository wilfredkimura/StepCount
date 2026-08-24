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
    const val KEY_LAST_HARDWARE_TIMESTAMP = "key_last_hardware_timestamp"
    const val KEY_LAST_RECORDED_DATE = "key_last_recorded_date"
    const val KEY_DARK_MODE = "key_dark_mode"
    const val KEY_STEP_UNITS = "key_step_units"
    const val KEY_GUEST_MODE = "key_guest_mode"

    // Goal and milestone notification preferences
    const val KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled"
    const val KEY_MILESTONE_PERCENTAGE = "key_milestone_percentage"
    const val KEY_LAST_NOTIFIED_DATE_MILESTONE = "key_last_notified_date_milestone"
    const val KEY_LAST_NOTIFIED_DATE_GOAL = "key_last_notified_date_goal"

    // Default custom milestone percentage (e.g. 50% of daily goal)
    const val DEFAULT_MILESTONE_PERCENTAGE = 50

    // Notification Channel ID and Name for daily goals and milestones
    const val NOTIFICATION_CHANNEL_GOALS = "stepcount_goals_channel"
    const val NOTIFICATION_CHANNEL_GOALS_NAME = "Daily Goal & Milestone Alerts"

    // 24/7 Background Foreground Tracking constants
    const val KEY_PERSISTENT_TRACKING_ENABLED = "key_persistent_tracking_enabled"
    const val NOTIFICATION_CHANNEL_FOREGROUND_TRACKING = "stepcount_foreground_tracking_channel"
    const val NOTIFICATION_CHANNEL_FOREGROUND_TRACKING_NAME = "24/7 Step Tracking Service"

    // Backend Base URL (delegates to AppConfig for easy local vs Render switching)
    val BACKEND_BASE_URL: String
        get() = AppConfig.BASE_URL
}
