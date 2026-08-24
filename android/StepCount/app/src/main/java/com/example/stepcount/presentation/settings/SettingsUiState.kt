package com.example.stepcount.presentation.settings

import com.example.stepcount.core.theme.AppThemeMode

/**
 * UI State for the Settings screen.
 */
data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isDarkMode: Boolean = false,
    val isKilometers: Boolean = true,
    val isAutoCloudSyncEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isPersistentTrackingEnabled: Boolean = true,
    val milestonePercentage: Int = 50,
    val isSyncingNow: Boolean = false,
    val syncSuccessMessage: String? = null,
    val showClearHistoryDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val isLoggedOut: Boolean = false
)
