package com.example.stepcount.domain.usecase

import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.StepRepository

/**
 * Use case to delete a specific day's record from history.
 */
class DeleteStepRecordUseCase(
    private val stepRepository: StepRepository
) {
    suspend operator fun invoke(date: String): Resource<Unit> {
        return stepRepository.deleteStepRecord(date)
    }
}
