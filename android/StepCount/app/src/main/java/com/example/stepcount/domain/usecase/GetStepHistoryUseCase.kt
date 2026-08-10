package com.example.stepcount.domain.usecase

import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Use case to retrieve and optionally filter the user's historical step records.
 */
class GetStepHistoryUseCase(
    private val stepRepository: StepRepository
) {
    /**
     * Retrieves history records with optional filter ("all", "week", "month").
     */
    operator fun invoke(filter: String = "all"): Flow<List<DailyStepRecord>> {
        return stepRepository.getAllStepHistory().map { records ->
            when (filter.lowercase(Locale.US)) {
                "week" -> {
                    val sevenDaysAgo = getDateDaysAgo(7)
                    records.filter { it.date >= sevenDaysAgo }
                }
                "month" -> {
                    val thirtyDaysAgo = getDateDaysAgo(30)
                    records.filter { it.date >= thirtyDaysAgo }
                }
                else -> records
            }
        }
    }

    private fun getDateDaysAgo(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(calendar.time)
    }
}
