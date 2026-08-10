package com.example.stepcount.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.stepcount.data.local.entity.LeaderboardCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for reading and refreshing cached leaderboard rankings.
 */
@Dao
interface LeaderboardCacheDao {

    /**
     * Observes cached leaderboard rankings for a specific timeframe (today, week, all_time).
     */
    @Query("SELECT * FROM leaderboard_cache WHERE period = :period ORDER BY rank ASC")
    fun getLeaderboardByPeriod(period: String): Flow<List<LeaderboardCacheEntity>>

    /**
     * Inserts fresh leaderboard entries.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LeaderboardCacheEntity>)

    /**
     * Clears cached rankings for a specific timeframe.
     */
    @Query("DELETE FROM leaderboard_cache WHERE period = :period")
    suspend fun clearPeriodCache(period: String)

    /**
     * Atomically replaces the cached leaderboard rankings for a period in a single transaction.
     */
    @Transaction
    suspend fun replaceLeaderboardCache(period: String, entries: List<LeaderboardCacheEntity>) {
        clearPeriodCache(period)
        insertAll(entries)
    }

    /**
     * Clears all cached rankings.
     */
    @Query("DELETE FROM leaderboard_cache")
    suspend fun clearAll()
}
