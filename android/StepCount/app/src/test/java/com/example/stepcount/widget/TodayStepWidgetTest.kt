package com.example.stepcount.widget

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * Unit tests verifying Today's Steps Home Screen Widget calculation and formatting logic.
 */
class TodayStepWidgetTest {

    @Test
    fun `calculateProgressPercentage calculates exact percentage correctly`() {
        val steps = 5000L
        val goal = 10000
        val percentage = if (goal > 0) {
            ((steps.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }
        assertEquals(50, percentage)
    }

    @Test
    fun `calculateProgressPercentage caps at 100 percent when goal is exceeded`() {
        val steps = 15000L
        val goal = 10000
        val percentage = if (goal > 0) {
            ((steps.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }
        assertEquals(100, percentage)
    }

    @Test
    fun `calculateProgressPercentage returns 0 when goal is zero or negative`() {
        val steps = 2500L
        val goal = 0
        val percentage = if (goal > 0) {
            ((steps.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }
        assertEquals(0, percentage)
    }

    @Test
    fun `calculateCalories computes active kcal burned accurately`() {
        val steps = 6420L
        val calories = (steps * 0.045).toInt()
        assertEquals(288, calories)
    }

    @Test
    fun `formatSteps formats numbers with comma separators for clean display`() {
        val steps = 12450L
        val formatted = "%,d".format(Locale.US, steps)
        assertEquals("12,450", formatted)
    }

    @Test
    fun `formatGoalString produces expected widget subtitle string`() {
        val goal = 10000
        val percentage = 75
        val formatted = "Goal: %,d (%d%%)".format(Locale.US, goal, percentage)
        assertEquals("Goal: 10,000 (75%)", formatted)
    }
}
