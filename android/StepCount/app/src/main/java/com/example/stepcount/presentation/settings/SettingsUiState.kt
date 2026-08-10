package com.example.stepcount.presentation.settings

/**
 * UI State for the Settings screen.
 */
data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isKilometers: Boolean = true,
    val isSyncingNow: Boolean = false,
    val syncSuccessMessage: String? = null,
    val showClearHistoryDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val isLoggedOut: Boolean = false
)
