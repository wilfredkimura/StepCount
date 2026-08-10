package com.example.stepcount.data.repository

import com.example.stepcount.data.local.dao.DailyStepsDao
import com.example.stepcount.data.local.dao.UserProfileDao
import com.example.stepcount.data.local.entity.DailyStepsEntity
import com.example.stepcount.data.remote.api.StepCountApiService
import com.example.stepcount.data.remote.auth.FirebaseAuthService
import com.example.stepcount.data.remote.dto.StepUploadRequestDto
import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.StepRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository implementation for managing daily step recording, offline caching, and remote synchronization.
 */
class StepRepositoryImpl(
    private val authService: FirebaseAuthService,
    private val apiService: StepCountApiService,
    private val dailyStepsDao: DailyStepsDao,
    private val userProfileDao: UserProfileDao
) : StepRepository {

    private fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }

    override fun getTodaySteps(): Flow<DailyStepRecord?> {
        val userId = authService.getCurrentUserId() ?: ""
        val today = getTodayDateString()
        return dailyStepsDao.getStepsForDate(userId, today).map { entity ->
            entity?.let {
                DailyStepRecord(
                    id = it.id,
                    userId = it.userId,
                    date = it.date,
                    steps = it.steps,
                    goal = it.goal,
                    isSynced = it.synced
                )
            }
        }
    }

    override fun getAllStepHistory(): Flow<List<DailyStepRecord>> {
        val userId = authService.getCurrentUserId() ?: ""
        return dailyStepsDao.getAllStepsHistory(userId).map { list ->
            list.map {
                DailyStepRecord(
                    id = it.id,
                    userId = it.userId,
                    date = it.date,
                    steps = it.steps,
                    goal = it.goal,
                    isSynced = it.synced
                )
            }
        }
    }

    override suspend fun saveDailySteps(date: String, steps: Long): Resource<Unit> = withContext(Dispatchers.IO) {
        val userId = authService.getCurrentUserId() ?: return@withContext Resource.Error("User not logged in")
        try {
            val existing = dailyStepsDao.getStepsForDateOnce(userId, date)
            val profile = userProfileDao.getUserProfileOnce(userId)
            val goal = profile?.dailyGoal ?: 8000

            val entity = DailyStepsEntity(
                id = existing?.id ?: 0,
                userId = userId,
                date = date,
                steps = steps,
                goal = goal,
                synced = false // Always write locally first with synced = false
            )
            dailyStepsDao.upsertDailySteps(entity)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save daily steps")
        }
    }

    override suspend fun deleteStepRecord(date: String): Resource<Unit> = withContext(Dispatchers.IO) {
        val userId = authService.getCurrentUserId() ?: return@withContext Resource.Error("User not logged in")
        try {
            dailyStepsDao.deleteRecordByDate(userId, date)
            if (!authService.isGuestMode()) {
                try {
                    apiService.deleteStepsForDate(date)
                } catch (e: Exception) {
                    // Ignore remote failure when offline
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete step record")
        }
    }

    override suspend fun clearAllHistory(): Resource<Unit> = withContext(Dispatchers.IO) {
        val userId = authService.getCurrentUserId() ?: return@withContext Resource.Error("User not logged in")
        try {
            dailyStepsDao.clearAllHistory(userId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to clear history")
        }
    }

    override suspend fun syncPendingSteps(): Resource<Int> = withContext(Dispatchers.IO) {
        if (authService.isGuestMode()) {
            return@withContext Resource.Success(0) // No remote syncing in guest mode
        }
        val userId = authService.getCurrentUserId() ?: return@withContext Resource.Error("User not logged in")
        val unsyncedList = dailyStepsDao.getUnsyncedRecords(userId)
        if (unsyncedList.isEmpty()) {
            return@withContext Resource.Success(0)
        }

        var syncedCount = 0
        for (record in unsyncedList) {
            try {
                val response = apiService.uploadDailySteps(
                    StepUploadRequestDto(
                        date = record.date,
                        steps = record.steps,
                        goal = record.goal
                    )
                )
                if (response.isSuccessful) {
                    dailyStepsDao.markAsSynced(record.id)
                    syncedCount++
                }
            } catch (e: Exception) {
                // Break on connection loss
                break
            }
        }
        Resource.Success(syncedCount)
    }

    override fun getTotalLifetimeSteps(): Flow<Long> {
        val userId = authService.getCurrentUserId() ?: ""
        return dailyStepsDao.getTotalStepsCount(userId)
    }
}
