package com.example.stepcount.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores a local cache of leaderboard rankings in the Room database.
 * This table allows users to browse competitive standings even when offline.
 */
@Entity(tableName = "leaderboard_cache")
data class LeaderboardCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,   // Firebase UID of the ranked user
    val name: String,     // Display name of the user
    val steps: Long,      // Total steps or score in the selected period
    val rank: Int,        // Position ranking (1, 2, 3, etc.)
    val period: String,   // Timeframe of ranking: "today", "week", or "all_time"
    val lastUpdated: Long // Timestamp in milliseconds when this ranking was cached
)
