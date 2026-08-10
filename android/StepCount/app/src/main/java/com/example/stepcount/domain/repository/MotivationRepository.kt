package com.example.stepcount.domain.repository

import com.example.stepcount.domain.model.MotivationalQuote
import com.example.stepcount.domain.model.Resource

/**
 * Interface for fetching daily inspirational quotes.
 */
interface MotivationRepository {
    /**
     * Retrieves the daily motivational quote.
     */
    suspend fun getMotivationalQuote(): Resource<MotivationalQuote>
}
