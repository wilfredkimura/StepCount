package com.example.stepcount.data.repository

import com.example.stepcount.data.local.dao.DailyStepsDao
import com.example.stepcount.data.local.dao.UserProfileDao
import com.example.stepcount.data.local.entity.DailyStepsEntity
import com.example.stepcount.data.local.entity.UserProfileEntity
import com.example.stepcount.data.remote.api.StepCountApiService
import com.example.stepcount.data.remote.auth.FirebaseAuthService
import com.example.stepcount.data.remote.dto.StepResponseDto
import com.example.stepcount.domain.model.Resource
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Unit tests verifying StepRepositoryImpl offline-first caching, saving, and batch syncing.
 */
class StepRepositoryTest {

    private lateinit var authService: FirebaseAuthService
    private lateinit var apiService: StepCountApiService
    private lateinit var dailyStepsDao: DailyStepsDao
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var repository: StepRepositoryImpl

    @Before
    fun setUp() {
        authService = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        dailyStepsDao = mockk(relaxed = true)
        userProfileDao = mockk(relaxed = true)

        every { authService.getCurrentUserId() } returns "user_100"
        every { authService.isGuestMode() } returns false

        repository = StepRepositoryImpl(
            authService = authService,
            apiService = apiService,
            dailyStepsDao = dailyStepsDao,
            userProfileDao = userProfileDao
        )
    }

    @Test
    fun saveDailySteps_writesToRoomWithSyncedFalse() = runTest {
        coEvery { dailyStepsDao.getStepsForDateOnce("user_100", "2026-08-10") } returns null
        coEvery { userProfileDao.getUserProfileOnce("user_100") } returns UserProfileEntity("user_100", "a@b.com", "Alex", 10000)
        coEvery { dailyStepsDao.upsertDailySteps(any()) } returns 1L

        val result = repository.saveDailySteps("2026-08-10", 7500)

        assertTrue(result is Resource.Success)
        coVerify {
            dailyStepsDao.upsertDailySteps(
                match { it.userId == "user_100" && it.date == "2026-08-10" && it.steps == 7500L && !it.synced }
            )
        }
    }

    @Test
    fun syncPendingSteps_uploadsUnsyncedRecordsAndMarksSynced() = runTest {
        val unsynced = listOf(
            DailyStepsEntity(id = 10, userId = "user_100", date = "2026-08-09", steps = 8000, goal = 10000, synced = false)
        )
        coEvery { dailyStepsDao.getUnsyncedRecords("user_100") } returns unsynced
        coEvery { apiService.uploadDailySteps(any()) } returns Response.success(
            StepResponseDto(100, "user_100", "2026-08-09", 8000, 10000)
        )

        val result = repository.syncPendingSteps()

        assertTrue(result is Resource.Success)
        assertEquals(1, (result as Resource.Success).data)
        coVerify { dailyStepsDao.markAsSynced(10L) }
    }
}
