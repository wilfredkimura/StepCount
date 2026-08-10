package com.example.stepcount.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepcount.domain.repository.AuthRepository
import com.example.stepcount.domain.repository.StepRepository
import com.example.stepcount.domain.usecase.UpdateDailyGoalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing user profile details, goal modifications, and lifetime walking stats.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val stepRepository: StepRepository,
    private val updateDailyGoalUseCase: UpdateDailyGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
        observeLifetimeSteps()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { profile ->
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            userProfile = profile,
                            currentGoal = profile.dailyGoal,
                            tempGoalInput = profile.dailyGoal.toString()
                        )
                    }
                }
            }
        }
    }

    private fun observeLifetimeSteps() {
        viewModelScope.launch {
            stepRepository.getTotalLifetimeSteps().collect { total ->
                _uiState.update { it.copy(totalLifetimeSteps = total) }
            }
        }
    }

    fun onGoalInputChange(input: String) {
        _uiState.update { it.copy(tempGoalInput = input) }
    }

    fun selectPresetGoal(preset: Int) {
        _uiState.update { it.copy(tempGoalInput = preset.toString()) }
        saveGoal(preset)
    }

    fun saveGoal(goal: Int = _uiState.value.tempGoalInput.toIntOrNull() ?: 8000) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            updateDailyGoalUseCase(goal)
            _uiState.update {
                it.copy(
                    currentGoal = goal,
                    isLoading = false,
                    isEditingGoal = false,
                    isSavedSuccess = true
                )
            }
        }
    }

    fun toggleEditGoal(isEditing: Boolean) {
        _uiState.update { it.copy(isEditingGoal = isEditing) }
    }
}
