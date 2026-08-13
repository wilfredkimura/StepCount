package com.example.stepcount.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * ViewModel managing user preferences, manual synchronization, and account deletion.
 */
class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val stepRepository: StepRepository,
    private val syncPendingStepsUseCase: SyncPendingStepsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // One-shot event channel for UI Toast notifications
    private val _toastEventChannel = Channel<String>(Channel.BUFFERED)
    val toastMessageEvent: Flow<String> = _toastEventChannel.receiveAsFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleUnits(isKm: Boolean) {
        _uiState.update { it.copy(isKilometers = isKm) }
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
