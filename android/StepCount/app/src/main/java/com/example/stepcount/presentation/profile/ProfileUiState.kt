package com.example.stepcount.presentation.profile

import com.example.stepcount.domain.model.UserProfile

/**
 * UI State for the Profile screen.
 */
data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val currentGoal: Int = 8000,
    val totalLifetimeSteps: Long = 0L,
    val isEditingGoal: Boolean = false,
    val tempGoalInput: String = "8000",
    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false
) {
    val totalDistanceKm: Double
        get() = (totalLifetimeSteps * 0.762) / 1000.0

    val totalCalories: Long
        get() = (totalLifetimeSteps * 0.04).toLong()
}
