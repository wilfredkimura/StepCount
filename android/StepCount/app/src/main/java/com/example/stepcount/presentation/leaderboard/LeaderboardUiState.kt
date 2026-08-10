package com.example.stepcount.presentation.leaderboard

import com.example.stepcount.domain.model.LeaderboardEntry

/**
 * UI State for the Leaderboard rankings screen.
 */
data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val activePeriod: String = "today", // "today", "week", "all_time"
    val isRefreshing: Boolean = false,
    val isOfflineCache: Boolean = false,
    val errorMessage: String? = null
) {
    val topThree: List<LeaderboardEntry>
        get() = entries.take(3)

    val restOfLeaderboard: List<LeaderboardEntry>
        get() = if (entries.size > 3) entries.drop(3) else emptyList()
}
