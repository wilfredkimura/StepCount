package com.example.stepcount.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.AuthRepository
import com.example.stepcount.domain.usecase.GetMotivationalQuoteUseCase
import com.example.stepcount.domain.usecase.GetTodayStepsUseCase
import com.example.stepcount.domain.usecase.RecordStepDeltaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.stepcount.domain.usecase.GetStreakUseCase

/**
 * ViewModel managing live step counting, daily goal metrics, and motivational quotes.
 */
class DashboardViewModel(
    private val getTodayStepsUseCase: GetTodayStepsUseCase,
    private val recordStepDeltaUseCase: RecordStepDeltaUseCase,
    private val getMotivationalQuoteUseCase: GetMotivationalQuoteUseCase,
    private val getStreakUseCase: GetStreakUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
        observeTodaySteps()
        observeStreak()
        fetchMotivationalQuote()
    }

    private fun observeStreak() {
        viewModelScope.launch {
            getStreakUseCase().collect { streakInfo ->
                _uiState.update { it.copy(streakInfo = streakInfo) }
            }
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            dailyGoal = user.dailyGoal,
                            isGuestMode = user.email == "guest@local"
                        )
                    }
                }
            }
        }
    }

    private fun observeTodaySteps() {
        viewModelScope.launch {
            getTodayStepsUseCase().collect { record ->
                if (record != null) {
                    _uiState.update {
                        it.copy(
                            todayRecord = record,
                            liveSteps = record.steps,
                            dailyGoal = record.goal
                        )
                    }
                }
            }
        }
    }

    fun onStepCountUpdatedFromSensor(steps: Long) {
        _uiState.update { it.copy(liveSteps = steps) }
    }

    fun setFallbackSensorActive(isActive: Boolean) {
        _uiState.update { it.copy(isFallbackSensorActive = isActive) }
    }

    fun fetchMotivationalQuote() {
        viewModelScope.launch {
            when (val result = getMotivationalQuoteUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(motivationalQuote = result.data) }
                }
                else -> { /* Retain previous quote */ }
            }
        }
    }
}
