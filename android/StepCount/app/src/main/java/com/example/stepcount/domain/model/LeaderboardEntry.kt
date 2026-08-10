package com.example.stepcount.domain.model

/**
 * Pure domain model representing a ranked user on the leaderboard.
 */
data class LeaderboardEntry(
    val userId: String,
    val name: String,
    val steps: Long,
    val rank: Int,
    val goalPercentage: Float = 0f,
    val isCurrentUser: Boolean = false
)
