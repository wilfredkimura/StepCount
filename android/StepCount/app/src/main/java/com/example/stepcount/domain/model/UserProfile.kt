package com.example.stepcount.domain.model

import com.example.stepcount.core.util.Constants

/**
 * Pure domain model representing the current user's profile.
 * Contains user identity and target daily walking goal.
 */
data class UserProfile(
    val userId: String,
    val email: String,
    val name: String,
    val dailyGoal: Int = Constants.DEFAULT_DAILY_GOAL
)
