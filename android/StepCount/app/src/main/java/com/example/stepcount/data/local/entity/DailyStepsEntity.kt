package com.example.stepcount.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.stepcount.core.util.Constants

/**
 * Stores the user's daily step records in the local Room database.
 * If the user profile is deleted, all associated daily step records are deleted automatically (cascade delete).
 * The unique index on (userId, date) prevents duplicate rows for the same user on the same calendar day.
 */
@Entity(
    tableName = "daily_steps",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "date"], unique = true)
    ]
)
data class DailyStepsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val date: String, // Calendar date in standard format: yyyy-MM-dd
    val steps: Long,  // Total steps walked on this specific day
    val goal: Int = Constants.DEFAULT_DAILY_GOAL, // The daily goal that was active on that day
    val synced: Boolean = false // Tracks if this record has been uploaded to the backend server
)
