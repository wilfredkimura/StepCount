package com.example.stepcount.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.stepcount.core.util.Constants
import com.example.stepcount.data.local.dao.DailyStepsDao
import com.example.stepcount.data.local.dao.LeaderboardCacheDao
import com.example.stepcount.data.local.dao.UserProfileDao
import com.example.stepcount.data.local.entity.DailyStepsEntity
import com.example.stepcount.data.local.entity.LeaderboardCacheEntity
import com.example.stepcount.data.local.entity.UserProfileEntity

/**
 * Main Room Database for the StepCount application.
 * Manages tables for user profile, daily step records, and cached leaderboard rankings.
 * Uses a thread-safe singleton pattern to ensure only one database instance is created.
 */
@Database(
    entities = [
        UserProfileEntity::class,
        DailyStepsEntity::class,
        LeaderboardCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StepCountDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyStepsDao(): DailyStepsDao
    abstract fun leaderboardCacheDao(): LeaderboardCacheDao

    companion object {
        @Volatile
        private var INSTANCE: StepCountDatabase? = null

        /**
         * Returns the singleton database instance, creating it if it doesn't exist yet.
         */
        fun getDatabase(context: Context): StepCountDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StepCountDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Creates an in-memory database instance used exclusively for automated unit tests.
         */
        fun createInMemoryDatabase(context: Context): StepCountDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                StepCountDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        }
    }
}
