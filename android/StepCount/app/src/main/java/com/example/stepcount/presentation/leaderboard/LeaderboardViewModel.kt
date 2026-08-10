package com.example.stepcount.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepcount.domain.usecase.GetLeaderboardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing leaderboard rankings, period tabs, and server refreshes.
 */
class LeaderboardViewModel(
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        switchPeriod("today")
    }

    fun switchPeriod(period: String) {
        _uiState.update { it.copy(activePeriod = period) }
        observeLeaderboard(period)
        refresh(period)
    }

    private fun observeLeaderboard(period: String) {
        viewModelScope.launch {
            getLeaderboardUseCase(period).collect { list ->
                _uiState.update {
                    it.copy(
                        entries = list,
                        isOfflineCache = list.isNotEmpty()
                    )
                }
            }
        }
    }

    fun refresh(period: String = _uiState.value.activePeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                getLeaderboardUseCase.refresh(period)
                _uiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = "Showing cached standings (Offline)"
                    )
                }
            }
        }
    }
}
