package com.example.stepcount.domain.repository

import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interface for reading, recording, and synchronizing step records.
 */
interface StepRepository {
    /**
     * Observes step progress for today in real-time.
     */
    fun getTodaySteps(): Flow<DailyStepRecord?>

    /**
     * Observes the user's complete history of daily step records.
     */
    fun getAllStepHistory(): Flow<List<DailyStepRecord>>

    /**
     * Saves or increments step count for a given date.
     */
    suspend fun saveDailySteps(date: String, steps: Long): Resource<Unit>

    /**
     * Deletes a specific day's record from local history and remote backend.
     */
    suspend fun deleteStepRecord(date: String): Resource<Unit>

    /**
     * Clears all local step history records.
     */
    suspend fun clearAllHistory(): Resource<Unit>

    /**
     * Uploads pending local step records (synced = false) to the backend server.
     */
    suspend fun syncPendingSteps(): Resource<Int>

    /**
     * Observes lifetime total steps walked.
     */
    fun getTotalLifetimeSteps(): Flow<Long>
}
