package com.example.stepcount.presentation.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepcount.core.notification.StepNotificationHelper
import com.example.stepcount.core.util.Constants
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.AuthRepository
import com.example.stepcount.domain.repository.StepRepository
import com.example.stepcount.domain.usecase.SyncPendingStepsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing user preferences, goal notification milestones, manual synchronization, and account management.
 */
class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val stepRepository: StepRepository,
    private val syncPendingStepsUseCase: SyncPendingStepsUseCase,
    private val notificationHelper: StepNotificationHelper? = null,
    private val prefs: SharedPreferences? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // One-shot event channel for UI Toast notifications
    private val _toastEventChannel = Channel<String>(Channel.BUFFERED)
    val toastMessageEvent: Flow<String> = _toastEventChannel.receiveAsFlow()

    init {
        loadPreferences()
    }

    /**
     * Loads saved user preferences from SharedPreferences into UI state.
     */
    private fun loadPreferences() {
        prefs?.let { p ->
            val isDark = p.getBoolean(Constants.KEY_DARK_MODE, false)
            val isKm = p.getBoolean(Constants.KEY_STEP_UNITS, true)
            val isNotifEnabled = p.getBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, true)
            val milestonePct = p.getInt(Constants.KEY_MILESTONE_PERCENTAGE, Constants.DEFAULT_MILESTONE_PERCENTAGE)

            _uiState.update {
                it.copy(
                    isDarkMode = isDark,
                    isKilometers = isKm,
                    isNotificationsEnabled = isNotifEnabled,
                    milestonePercentage = milestonePct
                )
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        prefs?.edit()?.putBoolean(Constants.KEY_DARK_MODE, enabled)?.apply()
    }

    fun toggleUnits(isKm: Boolean) {
        _uiState.update { it.copy(isKilometers = isKm) }
        prefs?.edit()?.putBoolean(Constants.KEY_STEP_UNITS, isKm)?.apply()
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationsEnabled = enabled) }
        prefs?.edit()?.putBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, enabled)?.apply()
    }

    fun setMilestonePercentage(percentage: Int) {
        val clamped = percentage.coerceIn(10, 95)
        _uiState.update { it.copy(milestonePercentage = clamped) }
        prefs?.edit()?.putInt(Constants.KEY_MILESTONE_PERCENTAGE, clamped)?.apply()
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            if (notificationHelper != null) {
                notificationHelper.sendTestNotification()
                _toastEventChannel.send("Test goal notification posted to notifications tray!")
            } else {
                _toastEventChannel.send("Notifications active: milestone alert set at ${_uiState.value.milestonePercentage}%")
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingNow = true, syncSuccessMessage = null) }
            when (val result = syncPendingStepsUseCase()) {
                is Resource.Success -> {
                    val count = result.data ?: 0
                    val message = if (count > 0) {
                        "Successfully synced $count step record(s) with the server."
                    } else {
                        "All step records are already up to date."
                    }
                    _uiState.update {
                        it.copy(
                            isSyncingNow = false,
                            syncSuccessMessage = message
                        )
                    }
                    _toastEventChannel.send(message)
                }
                is Resource.Error -> {
                    val errorMessage = result.message ?: "Sync failed. Please check your internet connection or try again later."
                    _uiState.update {
                        it.copy(
                            isSyncingNow = false,
                            syncSuccessMessage = errorMessage
                        )
                    }
                    _toastEventChannel.send(errorMessage)
                }
                else -> {
                    _uiState.update { it.copy(isSyncingNow = false) }
                }
            }
        }
    }


    fun promptClearHistory(show: Boolean) {
        _uiState.update { it.copy(showClearHistoryDialog = show) }
    }

    fun promptDeleteAccount(show: Boolean) {
        _uiState.update { it.copy(showDeleteAccountDialog = show) }
    }

    fun promptSignOut(show: Boolean) {
        _uiState.update { it.copy(showSignOutDialog = show) }
    }

    fun confirmClearHistory() {
        viewModelScope.launch {
            stepRepository.clearAllHistory()
            _uiState.update { it.copy(showClearHistoryDialog = false) }
        }
    }

    fun confirmSignOut() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(showSignOutDialog = false, isLoggedOut = true) }
        }
    }

    fun confirmDeleteAccount() {
        viewModelScope.launch {
            authRepository.deleteAccount()
            _uiState.update { it.copy(showDeleteAccountDialog = false, isLoggedOut = true) }
        }
    }
}
