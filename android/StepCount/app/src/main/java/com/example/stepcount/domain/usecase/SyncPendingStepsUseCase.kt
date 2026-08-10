package com.example.stepcount.domain.usecase

import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.StepRepository

/**
 * Use case to upload all local unsynced step records to the server.
 */
class SyncPendingStepsUseCase(
    private val stepRepository: StepRepository
) {
    suspend operator fun invoke(): Resource<Int> {
        return stepRepository.syncPendingSteps()
    }
}
