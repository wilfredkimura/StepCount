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

    private val database = StepCountDatabase.getDatabase(context)

    val authService = FirebaseAuthService(context)
    val apiService = RetrofitClient.createApiService(authService)

    val authRepository: AuthRepository = AuthRepositoryImpl(
        authService = authService,
        apiService = apiService,
        userProfileDao = database.userProfileDao(),
        dailyStepsDao = database.dailyStepsDao()
    )

    val stepRepository: StepRepository = StepRepositoryImpl(
        authService = authService,
        apiService = apiService,
        dailyStepsDao = database.dailyStepsDao(),
        userProfileDao = database.userProfileDao()
    )

    val leaderboardRepository: LeaderboardRepository = LeaderboardRepositoryImpl(
        authService = authService,
        apiService = apiService,
        leaderboardCacheDao = database.leaderboardCacheDao()
    )

    val motivationRepository: MotivationRepository = MotivationRepositoryImpl(
        apiService = apiService
    )

    // Use Cases
    val getTodayStepsUseCase = GetTodayStepsUseCase(stepRepository)
    val recordStepDeltaUseCase = RecordStepDeltaUseCase(stepRepository)
    val getStepHistoryUseCase = GetStepHistoryUseCase(stepRepository)
    val deleteStepRecordUseCase = DeleteStepRecordUseCase(stepRepository)
    val getLeaderboardUseCase = GetLeaderboardUseCase(leaderboardRepository)
    val getMotivationalQuoteUseCase = GetMotivationalQuoteUseCase(motivationRepository)
    val syncPendingStepsUseCase = SyncPendingStepsUseCase(stepRepository)
    val updateDailyGoalUseCase = UpdateDailyGoalUseCase(authRepository)
}
