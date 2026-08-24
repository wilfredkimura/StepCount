package com.example.stepcount.data.remote.api

import com.example.stepcount.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface defining endpoints exposed by the FastAPI backend server.
 */
interface StepCountApiService {

    /**
     * Registers a new Firebase-authenticated user explicitly in the PostgreSQL database.
     */
    @POST("auth/register")
    suspend fun registerUserWithBackend(
        @Body request: RegisterRequestDto
    ): Response<UserProfileDto>

    /**
     * Synchronizes a Firebase-authenticated user with the PostgreSQL database.
     */
    @POST("auth/firebase-login")
    suspend fun syncUserWithBackend(
        @Body request: FirebaseLoginRequestDto
    ): Response<UserProfileDto>

    /**
     * Uploads or updates a daily step count record.
     */
    @POST("steps")
    suspend fun uploadDailySteps(
        @Body request: StepUploadRequestDto
    ): Response<StepResponseDto>

    /**
     * Retrieves today's step count record from the server.
     */
    @GET("steps/today")
    suspend fun getTodaySteps(): Response<StepResponseDto>

    /**
     * Retrieves the user's complete historical step records from the server.
     */
    @GET("steps/history")
    suspend fun getStepHistory(): Response<List<StepResponseDto>>

    /**
     * Updates steps for a specific date.
     */
    @PUT("steps/{date}")
    suspend fun updateStepsForDate(
        @Path("date") date: String,
        @Body request: StepUploadRequestDto
    ): Response<StepResponseDto>

    /**
     * Deletes a step record for a specific date.
     */
    @DELETE("steps/{date}")
    suspend fun deleteStepsForDate(
        @Path("date") date: String
    ): Response<Unit>

    /**
     * Retrieves competitive leaderboard rankings.
     */
    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("period") period: String = "today",
        @Query("type") type: String = "steps"
    ): Response<List<LeaderboardItemDto>>

    /**
     * Retrieves the current user's profile from the server.
     */
    @GET("profile")
    suspend fun getUserProfile(): Response<UserProfileDto>

    /**
     * Updates the user's profile and daily goal.
     */
    @PUT("profile")
    suspend fun updateUserProfile(
        @Body request: UserProfileDto
    ): Response<UserProfileDto>

    /**
     * Retrieves a daily motivational quote.
     */
    @GET("motivation")
    suspend fun getMotivationalQuote(): Response<MotivationDto>
}
