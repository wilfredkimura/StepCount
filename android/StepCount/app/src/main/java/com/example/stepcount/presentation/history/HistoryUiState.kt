package com.example.stepcount.presentation.history

import com.example.stepcount.domain.model.DailyStepRecord

/**
 * UI State for the Step History screen.
 */
data class HistoryUiState(
    val records: List<DailyStepRecord> = emptyList(),
    val activeFilter: String = "all", // "all", "week", "month"
    val recordPendingDeletion: DailyStepRecord? = null,
    val isLoading: Boolean = false,
    val totalHistorySteps: Long = 0L
)
