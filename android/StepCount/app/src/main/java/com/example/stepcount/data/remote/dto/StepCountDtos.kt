package com.example.stepcount.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects for FastAPI backend requests and responses.
 */

// Request to synchronize Firebase user in PostgreSQL backend
data class FirebaseLoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String
)

// Request to register a new user in PostgreSQL backend
data class RegisterRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("daily_goal") val dailyGoal: Int = 8000
)

// User Profile Response
data class UserProfileDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("daily_goal") val dailyGoal: Int
)

// Request to upload or update daily steps
data class StepUploadRequestDto(
    @SerializedName("date") val date: String,
    @SerializedName("steps") val steps: Long,
    @SerializedName("goal") val goal: Int
)

// Step record response
data class StepResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("user_id") val userId: String,
    @SerializedName("date") val date: String,
    @SerializedName("steps") val steps: Long,
    @SerializedName("goal") val goal: Int
)

// Leaderboard entry item
data class LeaderboardItemDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("steps") val steps: Long,
    @SerializedName("rank") val rank: Int,
    @SerializedName("goal_percentage") val goalPercentage: Float? = 0f
)

// Daily motivational quote response from Quotable API / backend
data class MotivationDto(
    @SerializedName("quote") val quote: String,
    @SerializedName("author") val author: String
)
