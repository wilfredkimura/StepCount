package com.example.stepcount.domain.usecase

import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe today's live step record continuously.
 */
class GetTodayStepsUseCase(
    private val stepRepository: StepRepository
) {
    operator fun invoke(): Flow<DailyStepRecord?> {
        return stepRepository.getTodaySteps()
    }
}
