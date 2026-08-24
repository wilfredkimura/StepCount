package com.example.stepcount.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stepcount.core.di.AppContainer
import com.example.stepcount.presentation.auth.AuthScreen
import com.example.stepcount.presentation.auth.AuthViewModel
import com.example.stepcount.presentation.dashboard.DashboardScreen
import com.example.stepcount.presentation.dashboard.DashboardViewModel
import com.example.stepcount.presentation.history.HistoryScreen
import com.example.stepcount.presentation.history.HistoryViewModel
import com.example.stepcount.presentation.leaderboard.LeaderboardScreen
import com.example.stepcount.presentation.leaderboard.LeaderboardViewModel
import com.example.stepcount.presentation.profile.ProfileScreen
import com.example.stepcount.presentation.profile.ProfileViewModel
import com.example.stepcount.presentation.settings.SettingsScreen
import com.example.stepcount.presentation.settings.SettingsViewModel
import com.example.stepcount.presentation.splash.SplashScreen
import com.example.stepcount.presentation.splash.SplashViewModel

/**
 * Main Navigation Graph managing screen destinations and transitions across the application.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val container = remember { AppContainer(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            val splashViewModel: SplashViewModel = remember {
                SplashViewModel(container.authRepository)
            }
            SplashScreen(
                viewModel = splashViewModel,
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Authentication Screen
        composable(Screen.Auth.route) {
            val authViewModel: AuthViewModel = remember {
                AuthViewModel(container.authRepository)
            }
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Dashboard Screen
        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = remember {
                DashboardViewModel(
                    getTodayStepsUseCase = container.getTodayStepsUseCase,
                    recordStepDeltaUseCase = container.recordStepDeltaUseCase,
                    getMotivationalQuoteUseCase = container.getMotivationalQuoteUseCase,
                    getStreakUseCase = container.getStreakUseCase,
                    authRepository = container.authRepository
                )
            }
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onBottomNavNavigate = { route ->
                    if (route != Screen.Dashboard.route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // 4. History Screen
        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = remember {
                HistoryViewModel(
                    getStepHistoryUseCase = container.getStepHistoryUseCase,
                    deleteStepRecordUseCase = container.deleteStepRecordUseCase,
                    getStreakUseCase = container.getStreakUseCase
                )
            }
            HistoryScreen(
                viewModel = historyViewModel,
                onBottomNavNavigate = { route ->
                    if (route != Screen.History.route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // 5. Leaderboard Screen
        composable(Screen.Leaderboard.route) {
            val leaderboardViewModel: LeaderboardViewModel = remember {
                LeaderboardViewModel(container.getLeaderboardUseCase)
            }
            LeaderboardScreen(
                viewModel = leaderboardViewModel,
                onBottomNavNavigate = { route ->
                    if (route != Screen.Leaderboard.route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // 6. Profile Screen
        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = remember {
                ProfileViewModel(
                    authRepository = container.authRepository,
                    stepRepository = container.stepRepository,
                    updateDailyGoalUseCase = container.updateDailyGoalUseCase,
                    getStreakUseCase = container.getStreakUseCase
                )
            }
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onBottomNavNavigate = { route ->
                    if (route != Screen.Profile.route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // 7. Settings Screen
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = remember {
                SettingsViewModel(
                    authRepository = container.authRepository,
                    stepRepository = container.stepRepository,
                    syncPendingStepsUseCase = container.syncPendingStepsUseCase,
                    notificationHelper = container.stepNotificationHelper,
                    prefs = context.getSharedPreferences(com.example.stepcount.core.util.Constants.PREFS_NAME, android.content.Context.MODE_PRIVATE)
                )
            }
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
