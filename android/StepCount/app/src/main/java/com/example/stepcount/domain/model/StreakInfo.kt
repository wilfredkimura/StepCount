package com.example.stepcount.domain.model

/**
 * Domain model representing a user's daily goal completion streak metrics.
 *
 * @property currentStreak The number of consecutive active days the user has completed their daily step goal.
 * @property bestStreak The longest consecutive streak of completed daily goals in the user's history.
 * @property totalGoalDays The total lifetime number of days where the daily step goal was met.
 */
data class StreakInfo(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalGoalDays: Int = 0
)
