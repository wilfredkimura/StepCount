package com.example.stepcount.core.theme

/**
 * Supported UI theme modes for StepCount.
 */
enum class AppThemeMode(val storageKey: String, val title: String) {
    SYSTEM("SYSTEM", "System"),
    LIGHT("LIGHT", "Light"),
    DARK("DARK", "Dark");

    companion object {
        fun fromStorageKey(key: String?): AppThemeMode {
            return entries.find { it.storageKey.equals(key, ignoreCase = true) } ?: SYSTEM
        }
    }
}
