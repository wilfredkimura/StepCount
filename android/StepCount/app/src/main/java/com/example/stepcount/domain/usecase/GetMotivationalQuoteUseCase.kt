package com.example.stepcount.domain.usecase

import com.example.stepcount.domain.model.MotivationalQuote
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.MotivationRepository

/**
 * Use case to fetch the daily motivational quote.
 */
class GetMotivationalQuoteUseCase(
    private val motivationRepository: MotivationRepository
) {
    suspend operator fun invoke(): Resource<MotivationalQuote> {
        return motivationRepository.getMotivationalQuote()
    }
}
