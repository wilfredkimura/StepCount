package com.example.stepcount.domain.repository

import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining authentication and user profile management actions.
 */
interface AuthRepository {
    /**
     * Observes the currently logged-in user profile from local storage.
     */
    fun getCurrentUser(): Flow<UserProfile?>

    /**
     * Registers a new user with email, password, and display name.
     */
    suspend fun register(email: String, password: String, name: String): Resource<UserProfile>

    /**
     * Authenticates an existing user with email and password.
     */
    suspend fun login(email: String, password: String): Resource<UserProfile>

    /**
     * Activates guest mode for offline tracking without an account.
     */
    suspend fun continueAsGuest(): UserProfile

    /**
     * Logs the user out and clears local session data.
     */
    suspend fun logout(): Resource<Unit>

    /**
     * Updates the user's daily step goal.
     */
    suspend fun updateDailyGoal(newGoal: Int): Resource<Unit>

    /**
     * Permanently deletes the account and all associated step records.
     */
    suspend fun deleteAccount(): Resource<Unit>
}
