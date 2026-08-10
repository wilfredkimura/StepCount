package com.example.stepcount.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.stepcount.data.local.entity.DailyStepsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for managing daily step counts and history in the local Room database.
 */
@Dao
interface DailyStepsDao {

    /**
     * Observes step records for a specific day as a reactive Flow.
     * Emits fresh data in real-time as steps increase throughout the day.
     */
    @Query("SELECT * FROM daily_steps WHERE userId = :userId AND date = :date LIMIT 1")
    fun getStepsForDate(userId: String, date: String): Flow<DailyStepsEntity?>

    /**
     * Single-shot read for a specific date's step count.
     */
    @Query("SELECT * FROM daily_steps WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getStepsForDateOnce(userId: String, date: String): DailyStepsEntity?

    /**
     * Observes the user's entire step history in descending order by date (most recent first).
     */
    @Query("SELECT * FROM daily_steps WHERE userId = :userId ORDER BY date DESC")
    fun getAllStepsHistory(userId: String): Flow<List<DailyStepsEntity>>

    /**
     * Retrieves all records that have not yet been synchronized with the remote backend (synced = false).
     */
    @Query("SELECT * FROM daily_steps WHERE userId = :userId AND synced = 0")
    suspend fun getUnsyncedRecords(userId: String): List<DailyStepsEntity>

    /**
     * Inserts or updates a daily step record.
     */
    @Upsert
    suspend fun upsertDailySteps(record: DailyStepsEntity): Long

    /**
     * Marks a specific record as successfully uploaded to the backend server.
     */
    @Query("UPDATE daily_steps SET synced = 1 WHERE id = :recordId")
    suspend fun markAsSynced(recordId: Long)

    /**
     * Deletes a specific day's record from history.
     */
    @Query("DELETE FROM daily_steps WHERE userId = :userId AND date = :date")
    suspend fun deleteRecordByDate(userId: String, date: String)

    /**
     * Clears the user's complete local step history.
     */
    @Query("DELETE FROM daily_steps WHERE userId = :userId")
    suspend fun clearAllHistory(userId: String)

    /**
     * Computes the lifetime total steps walked by the user.
     */
    @Query("SELECT COALESCE(SUM(steps), 0) FROM daily_steps WHERE userId = :userId")
    fun getTotalStepsCount(userId: String): Flow<Long>
}
