package com.example.stepcount.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.stepcount.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for reading and updating the logged-in user's profile in Room.
 */
@Dao
interface UserProfileDao {

    /**
     * Observes the user profile continuously as a reactive Flow.
     * Updates automatically whenever the user changes their name or goal.
     */
    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>

    /**
     * Single-shot read query to get user profile.
     */
    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfileOnce(userId: String): UserProfileEntity?

    /**
     * Inserts or updates the user profile record.
     */
    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    /**
     * Updates only the daily step goal target for the user.
     */
    @Query("UPDATE user_profile SET dailyGoal = :newGoal WHERE userId = :userId")
    suspend fun updateDailyGoal(userId: String, newGoal: Int)

    /**
     * Deletes the user profile, triggering cascade deletion of all related step records.
     */
    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: String)
}
