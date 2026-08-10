package com.example.stepcount.domain.usecase

import com.example.stepcount.domain.model.LeaderboardEntry
import com.example.stepcount.domain.repository.LeaderboardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve and refresh leaderboard rankings.
 */
class GetLeaderboardUseCase(
    private val leaderboardRepository: LeaderboardRepository
) {
    /**
     * Observes rankings for a period ("today", "week", "all_time").
     */
    operator fun invoke(period: String): Flow<List<LeaderboardEntry>> {
        return leaderboardRepository.getLeaderboard(period)
    }

    /**
     * Triggers a fresh download of rankings from the server.
     */
    suspend fun refresh(period: String, rankBy: String = "steps") {
        leaderboardRepository.refreshLeaderboard(period, rankBy)
    }
}
