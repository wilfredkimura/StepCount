package com.example.stepcount.domain

import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.repository.StepRepository
import com.example.stepcount.domain.usecase.GetStreakUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetStreakUseCase verifying daily goal streak calculations.
 */
class GetStreakUseCaseTest {

    private lateinit var stepRepository: StepRepository
    private lateinit var getStreakUseCase: GetStreakUseCase

    @Before
    fun setUp() {
        stepRepository = mockk()
        getStreakUseCase = GetStreakUseCase(stepRepository)
    }

    @Test
    fun `calculateStreak with empty records returns zeros`() {
        val result = getStreakUseCase.calculateStreak(emptyList(), "2026-08-19")
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.bestStreak)
        assertEquals(0, result.totalGoalDays)
    }

    @Test
    fun `calculateStreak when today goal is met counts today and consecutive past days`() {
        val records = listOf(
            DailyStepRecord(userId = "user1", date = "2026-08-19", steps = 10000, goal = 8000), // Met
            DailyStepRecord(userId = "user1", date = "2026-08-18", steps = 9000, goal = 8000),  // Met
            DailyStepRecord(userId = "user1", date = "2026-08-17", steps = 8500, goal = 8000),  // Met
            DailyStepRecord(userId = "user1", date = "2026-08-16", steps = 5000, goal = 8000)   // Missed
        )

        val result = getStreakUseCase.calculateStreak(records, "2026-08-19")
        assertEquals(3, result.currentStreak)
        assertEquals(3, result.bestStreak)
        assertEquals(3, result.totalGoalDays)
    }

    @Test
    fun `calculateStreak when today is still ongoing preserves yesterday streak`() {
        val records = listOf(
            DailyStepRecord(userId = "user1", date = "2026-08-19", steps = 3000, goal = 8000),  // In progress (not met)
            DailyStepRecord(userId = "user1", date = "2026-08-18", steps = 9500, goal = 8000),  // Met
            DailyStepRecord(userId = "user1", date = "2026-08-17", steps = 10000, goal = 8000), // Met
            DailyStepRecord(userId = "user1", date = "2026-08-16", steps = 4000, goal = 8000)   // Missed
        )

        val result = getStreakUseCase.calculateStreak(records, "2026-08-19")
        assertEquals(2, result.currentStreak) // Preserves 2-day streak from yesterday
        assertEquals(2, result.bestStreak)
        assertEquals(2, result.totalGoalDays)
    }

    @Test
    fun `calculateStreak when yesterday was missed resets current streak to zero`() {
        val records = listOf(
            DailyStepRecord(userId = "user1", date = "2026-08-19", steps = 2000, goal = 8000), // In progress
            DailyStepRecord(userId = "user1", date = "2026-08-18", steps = 4000, goal = 8000), // Missed yesterday
            DailyStepRecord(userId = "user1", date = "2026-08-17", steps = 10000, goal = 8000) // Met 2 days ago
        )

        val result = getStreakUseCase.calculateStreak(records, "2026-08-19")
        assertEquals(0, result.currentStreak)
        assertEquals(1, result.bestStreak)
        assertEquals(1, result.totalGoalDays)
    }

    @Test
    fun `calculateStreak finds best streak across historical sequences`() {
        val records = listOf(
            // Current streak: 2 days (Aug 18, Aug 19)
            DailyStepRecord(userId = "user1", date = "2026-08-19", steps = 9000, goal = 8000),
            DailyStepRecord(userId = "user1", date = "2026-08-18", steps = 9000, goal = 8000),
            DailyStepRecord(userId = "user1", date = "2026-08-17", steps = 3000, goal = 8000), // Gap
            // Historical streak: 4 days (Aug 10 - Aug 13)
            DailyStepRecord(userId = "user1", date = "2026-08-13", steps = 8500, goal = 8000),
            DailyStepRecord(userId = "user1", date = "2026-08-12", steps = 8500, goal = 8000),
            DailyStepRecord(userId = "user1", date = "2026-08-11", steps = 8500, goal = 8000),
            DailyStepRecord(userId = "user1", date = "2026-08-10", steps = 8500, goal = 8000)
        )

        val result = getStreakUseCase.calculateStreak(records, "2026-08-19")
        assertEquals(2, result.currentStreak)
        assertEquals(4, result.bestStreak)
        assertEquals(6, result.totalGoalDays)
    }

    @Test
    fun `invoke emits streak info from repository flow`() = runBlocking {
        val records = listOf(
            DailyStepRecord(userId = "user1", date = "2026-08-19", steps = 10000, goal = 8000)
        )
        every { stepRepository.getAllStepHistory() } returns flowOf(records)

        val emittedStreak = getStreakUseCase().first()
        assertEquals(1, emittedStreak.totalGoalDays)
    }
}
