package com.example.stepcount.domain.repository

import com.example.stepcount.domain.model.LeaderboardEntry
import com.example.stepcount.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interface for fetching and caching competitive leaderboard rankings.
 */
interface LeaderboardRepository {
    /**
     * Observes cached leaderboard rankings for a period ("today", "week", "all_time").
     */
    fun getLeaderboard(period: String): Flow<List<LeaderboardEntry>>

    /**
     * Refreshes rankings from the backend and updates the local Room cache.
     */
    suspend fun refreshLeaderboard(period: String, rankBy: String = "steps"): Resource<Unit>
}
