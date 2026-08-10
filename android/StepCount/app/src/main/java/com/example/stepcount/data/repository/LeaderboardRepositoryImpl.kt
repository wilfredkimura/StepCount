package com.example.stepcount.data.repository

import com.example.stepcount.data.local.dao.LeaderboardCacheDao
import com.example.stepcount.data.local.entity.LeaderboardCacheEntity
import com.example.stepcount.data.remote.api.StepCountApiService
import com.example.stepcount.data.remote.auth.FirebaseAuthService
import com.example.stepcount.domain.model.LeaderboardEntry
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.LeaderboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository implementation for managing competitive leaderboard rankings with local Room caching.
 */
class LeaderboardRepositoryImpl(
    private val authService: FirebaseAuthService,
    private val apiService: StepCountApiService,
    private val leaderboardCacheDao: LeaderboardCacheDao
) : LeaderboardRepository {

    override fun getLeaderboard(period: String): Flow<List<LeaderboardEntry>> {
        val currentUserId = authService.getCurrentUserId() ?: ""
        return leaderboardCacheDao.getLeaderboardByPeriod(period).map { entities ->
            entities.map {
                LeaderboardEntry(
                    userId = it.userId,
                    name = it.name,
                    steps = it.steps,
                    rank = it.rank,
                    isCurrentUser = it.userId == currentUserId
                )
            }
        }
    }

    override suspend fun refreshLeaderboard(period: String, rankBy: String): Resource<Unit> = withContext(Dispatchers.IO) {
        if (authService.isGuestMode()) {
            return@withContext Resource.Success(Unit)
        }
        try {
            val response = apiService.getLeaderboard(period, rankBy)
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!
                val now = System.currentTimeMillis()
                val cacheEntities = items.map {
                    LeaderboardCacheEntity(
                        userId = it.userId,
                        name = it.name,
                        steps = it.steps,
                        rank = it.rank,
                        period = period,
                        lastUpdated = now
                    )
                }
                leaderboardCacheDao.replaceLeaderboardCache(period, cacheEntities)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to fetch leaderboard: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error while refreshing leaderboard")
        }
    }
}
