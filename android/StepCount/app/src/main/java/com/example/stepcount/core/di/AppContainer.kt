package com.example.stepcount.core.di

import android.content.Context
import com.example.stepcount.data.local.StepCountDatabase
import com.example.stepcount.data.remote.RetrofitClient
import com.example.stepcount.data.remote.auth.FirebaseAuthService
import com.example.stepcount.data.repository.*
import com.example.stepcount.domain.repository.*
import com.example.stepcount.domain.usecase.*

/**
 * Dependency container providing singletons and use case instances across the application.
 */
class AppContainer(context: Context) {

    val database = StepCountDatabase.getDatabase(context)

    val authService = FirebaseAuthService(context)
    val apiService = RetrofitClient.createApiService(authService)

    val authRepository: AuthRepository = AuthRepositoryImpl(
        authService = authService,
        apiService = apiService,
        userProfileDao = database.userProfileDao(),
        dailyStepsDao = database.dailyStepsDao(),
        context = context
    )

    val stepRepository: StepRepository = StepRepositoryImpl(
        authService = authService,
        apiService = apiService,
        dailyStepsDao = database.dailyStepsDao(),
        userProfileDao = database.userProfileDao(),
        context = context
    )

    val motivationRepository: MotivationRepository = MotivationRepositoryImpl(
        apiService = apiService
    )

    val stepNotificationHelper = com.example.stepcount.core.notification.StepNotificationHelper(
        context = context,
        prefs = context.getSharedPreferences(com.example.stepcount.core.util.Constants.PREFS_NAME, Context.MODE_PRIVATE),
        motivationRepository = motivationRepository
    )

    val stepDeltaTracker = com.example.stepcount.sensor.StepDeltaTracker(
        prefs = context.getSharedPreferences(com.example.stepcount.core.util.Constants.PREFS_NAME, Context.MODE_PRIVATE),
        stepRepository = stepRepository,
        notificationHelper = stepNotificationHelper,
        context = context
    )

    val leaderboardRepository: LeaderboardRepository = LeaderboardRepositoryImpl(
        authService = authService,
        apiService = apiService,
        leaderboardCacheDao = database.leaderboardCacheDao()
    )

    // Use Cases
    val getTodayStepsUseCase = GetTodayStepsUseCase(stepRepository)
    val recordStepDeltaUseCase = RecordStepDeltaUseCase(stepRepository)
    val getStepHistoryUseCase = GetStepHistoryUseCase(stepRepository)
    val deleteStepRecordUseCase = DeleteStepRecordUseCase(stepRepository)
    val getStreakUseCase = GetStreakUseCase(stepRepository)
    val getLeaderboardUseCase = GetLeaderboardUseCase(leaderboardRepository)
    val getMotivationalQuoteUseCase = GetMotivationalQuoteUseCase(motivationRepository)
    val syncPendingStepsUseCase = SyncPendingStepsUseCase(stepRepository)
    val updateDailyGoalUseCase = UpdateDailyGoalUseCase(authRepository)
}
