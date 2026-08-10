package com.example.stepcount.data.repository

import com.example.stepcount.data.remote.api.StepCountApiService
import com.example.stepcount.domain.model.MotivationalQuote
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.MotivationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository implementation for retrieving inspirational quotes from backend / Quotable API.
 */
class MotivationRepositoryImpl(
    private val apiService: StepCountApiService
) : MotivationRepository {

    // Default offline fallback quote
    private val defaultQuote = MotivationalQuote(
        quote = "The secret of getting ahead is getting started.",
        author = "Mark Twain"
    )

    override suspend fun getMotivationalQuote(): Resource<MotivationalQuote> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMotivationalQuote()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Resource.Success(MotivationalQuote(dto.quote, dto.author))
            } else {
                Resource.Success(defaultQuote)
            }
        } catch (e: Exception) {
            // Graceful fallback to default quote when offline
            Resource.Success(defaultQuote)
        }
    }
}
