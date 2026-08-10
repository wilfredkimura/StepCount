package com.example.stepcount.domain.usecase

import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.StepRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Use case to record fresh steps for today into the local database.
 */
class RecordStepDeltaUseCase(
    private val stepRepository: StepRepository
) {
    suspend operator fun invoke(steps: Long): Resource<Unit> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(Date())
        return stepRepository.saveDailySteps(today, steps)
    }
}
