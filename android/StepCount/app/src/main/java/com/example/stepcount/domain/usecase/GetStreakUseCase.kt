package com.example.stepcount.domain.usecase

import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.model.StreakInfo
import com.example.stepcount.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Use case that observes historical daily step records and calculates daily goal streaks.
 *
 * It calculates:
 * 1. Current Streak: Consecutive days with met goals leading up to today (or yesterday if today is still ongoing).
 * 2. Best Streak: The highest consecutive days streak ever achieved in history.
 * 3. Total Completed Days: Lifetime count of days where the daily goal was achieved.
 *
 * This calculation runs 100% offline from the local Room database.
 */
class GetStreakUseCase(
    private val stepRepository: StepRepository
) {

    /**
     * Observes step history and emits real-time streak statistics whenever step records change.
     */
    operator fun invoke(): Flow<StreakInfo> {
        return stepRepository.getAllStepHistory().map { records ->
            calculateStreak(records)
        }
    }

    /**
     * Pure calculation logic for computing streaks from a list of daily step records.
     * Can be tested easily with unit tests without needing Android context.
     *
     * @param records List of all historical daily step records.
     * @param referenceDate Today's calendar date in yyyy-MM-dd format (defaults to current date).
     * @return StreakInfo containing current streak, best streak, and total goal days.
     */
    fun calculateStreak(
        records: List<DailyStepRecord>,
        referenceDate: String = getTodayDateString()
    ): StreakInfo {
        if (records.isEmpty()) {
            return StreakInfo(currentStreak = 0, bestStreak = 0, totalGoalDays = 0)
        }

        // Map records by date for fast O(1) lookup
        val recordsByDate = records.associateBy { it.date }

        // Count total lifetime days where the goal was met
        val totalGoalDays = records.count { it.isGoalMet }

        // 1. Calculate Current Streak
        val todayRecord = recordsByDate[referenceDate]
        val yesterdayDate = getPreviousDateString(referenceDate)
        val yesterdayRecord = recordsByDate[yesterdayDate]

        var currentStreak = 0

        if (todayRecord != null && todayRecord.isGoalMet) {
            // Case A: Today's goal has already been achieved!
            // Count today (1) and walk backwards through consecutive previous days.
            currentStreak = 1
            var checkDate = yesterdayDate
            while (recordsByDate[checkDate]?.isGoalMet == true) {
                currentStreak++
                checkDate = getPreviousDateString(checkDate)
            }
        } else if (yesterdayRecord != null && yesterdayRecord.isGoalMet) {
            // Case B: Today is still in progress (goal not yet met), but yesterday was met.
            // Preserve the active streak starting from yesterday.
            currentStreak = 1
            var checkDate = getPreviousDateString(yesterdayDate)
            while (recordsByDate[checkDate]?.isGoalMet == true) {
                currentStreak++
                checkDate = getPreviousDateString(checkDate)
            }
        } else {
            // Case C: Neither today nor yesterday met the goal, so active streak is 0.
            currentStreak = 0
        }

        // 2. Calculate Best (Longest) Historical Streak
        // Get all unique dates where the goal was met and sort them in chronological order
        val goalMetDates = records
            .filter { it.isGoalMet }
            .map { it.date }
            .distinct()
            .sorted()

        var bestStreak = 0
        var currentSequence = 0
        var previousDate: String? = null

        for (date in goalMetDates) {
            if (previousDate == null) {
                currentSequence = 1
            } else {
                val expectedNextDate = getNextDateString(previousDate)
                if (date == expectedNextDate) {
                    currentSequence++
                } else {
                    currentSequence = 1
                }
            }
            if (currentSequence > bestStreak) {
                bestStreak = currentSequence
            }
            previousDate = date
        }

        // Best streak must be at least as high as current streak
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak
        }

        return StreakInfo(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalGoalDays = totalGoalDays
        )
    }

    /**
     * Helper to get today's date in yyyy-MM-dd format.
     */
    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    /**
     * Computes the previous calendar date (date - 1 day) in yyyy-MM-dd format.
     */
    fun getPreviousDateString(dateStr: String): String {
        return shiftDate(dateStr, -1)
    }

    /**
     * Computes the next calendar date (date + 1 day) in yyyy-MM-dd format.
     */
    fun getNextDateString(dateStr: String): String {
        return shiftDate(dateStr, 1)
    }

    private fun shiftDate(dateStr: String, days: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdf.parse(dateStr) ?: return dateStr
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, days)
        return sdf.format(cal.time)
    }
}
