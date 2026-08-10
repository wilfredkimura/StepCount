package com.example.stepcount.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stepcount.data.local.dao.DailyStepsDao
import com.example.stepcount.data.local.dao.UserProfileDao
import com.example.stepcount.data.local.entity.DailyStepsEntity
import com.example.stepcount.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Automated Unit Test verifying Room Database CRUD operations, reactive flows, and sync flags.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DailyStepsDaoTest {

    private lateinit var database: StepCountDatabase
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var dailyStepsDao: DailyStepsDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StepCountDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userProfileDao = database.userProfileDao()
        dailyStepsDao = database.dailyStepsDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertUserProfile_and_insertDailySteps_readsSuccessfully() = runBlocking {
        // Arrange: Insert parent user profile first
        val profile = UserProfileEntity(
            userId = "user_123",
            email = "alex@example.com",
            name = "Alex Johnson",
            dailyGoal = 10000
        )
        userProfileDao.upsertUserProfile(profile)

        // Act: Insert today's step record
        val stepRecord = DailyStepsEntity(
            userId = "user_123",
            date = "2026-08-10",
            steps = 8432,
            goal = 10000,
            synced = false
        )
        dailyStepsDao.upsertDailySteps(stepRecord)

        // Assert: Read back steps for today
        val readRecord = dailyStepsDao.getStepsForDate("user_123", "2026-08-10").first()
        assertNotNull(readRecord)
        assertEquals(8432L, readRecord?.steps)
        assertEquals(false, readRecord?.synced)
    }

    @Test
    fun markAsSynced_updatesFlagCorrectly() = runBlocking {
        val profile = UserProfileEntity(
            userId = "user_456",
            email = "sam@example.com",
            name = "Sam Wilson"
        )
        userProfileDao.upsertUserProfile(profile)

        val recordId = dailyStepsDao.upsertDailySteps(
            DailyStepsEntity(
                userId = "user_456",
                date = "2026-08-09",
                steps = 5000,
                synced = false
            )
        )

        // Act: Mark record as synced
        dailyStepsDao.markAsSynced(recordId)

        // Assert: Unsynced records count should be 0
        val unsyncedList = dailyStepsDao.getUnsyncedRecords("user_456")
        assertTrue(unsyncedList.isEmpty())
    }

    @Test
    fun deleteUserProfile_cascadesAndDeletesStepRecords() = runBlocking {
        val profile = UserProfileEntity(
            userId = "user_cascade",
            email = "cascade@example.com",
            name = "Cascade Test"
        )
        userProfileDao.upsertUserProfile(profile)

        dailyStepsDao.upsertDailySteps(
            DailyStepsEntity(
                userId = "user_cascade",
                date = "2026-08-08",
                steps = 12000
            )
        )

        // Act: Delete user profile
        userProfileDao.deleteUserProfile("user_cascade")

        // Assert: Daily steps history should be empty due to CASCADE delete
        val history = dailyStepsDao.getAllStepsHistory("user_cascade").first()
        assertTrue(history.isEmpty())
    }
}
