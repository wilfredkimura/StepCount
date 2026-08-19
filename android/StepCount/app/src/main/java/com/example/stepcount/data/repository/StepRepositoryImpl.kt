package com.example.stepcount.data.repository

import android.content.Context
import com.example.stepcount.data.local.dao.DailyStepsDao

import com.example.stepcount.data.local.dao.UserProfileDao
import com.example.stepcount.data.local.entity.DailyStepsEntity
import com.example.stepcount.data.remote.api.StepCountApiService
import com.example.stepcount.data.remote.auth.FirebaseAuthService
import com.example.stepcount.data.remote.dto.StepUploadRequestDto
import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.StepRepository
import com.example.stepcount.widget.TodayStepWidgetReceiver
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
    private val userProfileDao: UserProfileDao,
    private val context: Context? = null
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
            context?.let { TodayStepWidgetReceiver.notifyStepsUpdated(it) }
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
                } else if (response.code() in 500..599) {
                    return@withContext Resource.Error("Server is temporarily down (HTTP ${response.code()}). Please try again later.")
                } else if (response.code() == 401) {
                    return@withContext Resource.Error("Authentication session expired. Please sign in again.")
                } else {
                    return@withContext Resource.Error("Sync failed: ${response.message()} (Code: ${response.code()})")
                }
            } catch (e: java.net.UnknownHostException) {
                return@withContext Resource.Error("Cannot reach server. Please check your internet connection or verify the server is running.")
            } catch (e: java.net.ConnectException) {
                return@withContext Resource.Error("Cannot connect to backend server. Make sure the server is online or check your network.")
            } catch (e: java.net.SocketTimeoutException) {
                return@withContext Resource.Error("Connection timed out. Please check your internet connection and try again.")
            } catch (e: java.io.IOException) {
                return@withContext Resource.Error("Network error during sync. Please check your internet connection.")
            } catch (e: Exception) {
                return@withContext Resource.Error("Sync error: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
        Resource.Success(syncedCount)
    }

    override fun getTotalLifetimeSteps(): Flow<Long> {
        val userId = authService.getCurrentUserId() ?: ""
        return dailyStepsDao.getTotalStepsCount(userId)
    }
}
