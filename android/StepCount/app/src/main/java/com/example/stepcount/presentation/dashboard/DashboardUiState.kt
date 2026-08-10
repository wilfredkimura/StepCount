package com.example.stepcount.presentation.dashboard

import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.model.MotivationalQuote

/**
 * UI State for the Dashboard screen.
 */
data class DashboardUiState(
    val todayRecord: DailyStepRecord? = null,
    val liveSteps: Long = 0L,
    val dailyGoal: Int = 8000,
    val motivationalQuote: MotivationalQuote? = null,
    val isGuestMode: Boolean = false,
    val isFallbackSensorActive: Boolean = false,
    val isLoading: Boolean = false,
    val isGoalAchievedCelebrated: Boolean = false
) {
    val progressFraction: Float
        get() = if (dailyGoal > 0) (liveSteps.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f) else 0f

    val goalPercentage: Int
        get() = if (dailyGoal > 0) ((liveSteps.toFloat() / dailyGoal.toFloat()) * 100).toInt() else 0
}
