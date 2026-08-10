package com.example.stepcount.domain.usecase

import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.AuthRepository

/**
 * Use case to update the user's daily step goal target.
 */
class UpdateDailyGoalUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(newGoal: Int): Resource<Unit> {
        return authRepository.updateDailyGoal(newGoal)
    }
}
