package com.example.stepcount.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stepcount.core.util.Constants

/**
 * Represents the logged-in user's profile stored in the local Room database.
 * This table holds the user's identification, display name, and active daily step goal.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String, // Firebase Unique User Identifier (UID) or guest ID
    val email: String,
    val name: String,
    val dailyGoal: Int = Constants.DEFAULT_DAILY_GOAL
)
