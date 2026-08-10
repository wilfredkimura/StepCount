package com.example.stepcount.presentation.navigation

/**
 * Sealed class defining all navigation routes within the StepCount app.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Auth : Screen("auth")
    data object Dashboard : Screen("dashboard")
    data object History : Screen("history")
    data object Leaderboard : Screen("leaderboard")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
}
