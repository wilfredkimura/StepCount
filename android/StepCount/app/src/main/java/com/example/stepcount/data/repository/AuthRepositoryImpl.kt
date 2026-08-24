package com.example.stepcount.data.repository

import android.content.Context
import com.example.stepcount.data.local.dao.DailyStepsDao
import com.example.stepcount.data.local.dao.UserProfileDao
import com.example.stepcount.data.local.entity.UserProfileEntity
import com.example.stepcount.data.remote.api.StepCountApiService
import com.example.stepcount.data.remote.auth.FirebaseAuthService
import com.example.stepcount.data.remote.dto.FirebaseLoginRequestDto
import com.example.stepcount.data.remote.dto.RegisterRequestDto
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.model.UserProfile
import com.example.stepcount.domain.repository.AuthRepository
import com.example.stepcount.widget.TodayStepWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository implementation managing authentication and user profile synchronization.
 */
class AuthRepositoryImpl(
    private val authService: FirebaseAuthService,
    private val apiService: StepCountApiService,
    private val userProfileDao: UserProfileDao,
    private val dailyStepsDao: DailyStepsDao,
    private val context: Context? = null
) : AuthRepository {


    override fun getCurrentUser(): Flow<UserProfile?> {
        val currentId = authService.getCurrentUserId() ?: ""
        return userProfileDao.getUserProfile(currentId).map { entity ->
            entity?.let {
                UserProfile(
                    userId = it.userId,
                    email = it.email,
                    name = it.name,
                    dailyGoal = it.dailyGoal
                )
            }
        }
    }

    override suspend fun register(email: String, password: String, name: String): Resource<UserProfile> = withContext(Dispatchers.IO) {
        try {
            // 1. Create account in Firebase Auth
            val userId = authService.register(email, password, name)
            var profileEntity = UserProfileEntity(
                userId = userId,
                email = email,
                name = name,
                dailyGoal = 8000
            )

            // 2. Explicitly provision user in PostgreSQL backend database
            try {
                val response = apiService.registerUserWithBackend(RegisterRequestDto(email, name, 8000))
                if (response.isSuccessful) {
                    val remoteProfile = response.body()
                    if (remoteProfile != null) {
                        profileEntity = UserProfileEntity(
                            userId = if (remoteProfile.userId.isNotBlank()) remoteProfile.userId else userId,
                            email = if (remoteProfile.email.isNotBlank()) remoteProfile.email else email,
                            name = if (remoteProfile.name.isNotBlank()) remoteProfile.name else name,
                            dailyGoal = remoteProfile.dailyGoal
                        )
                    }
                }
            } catch (e: Exception) {
                // Offline fallback: log and use local profile
                android.util.Log.w("AuthRepositoryImpl", "Could not reach backend during register, proceeding with local profile", e)
            }

            // Save confirmed profile in local Room database
            userProfileDao.upsertUserProfile(profileEntity)
            Resource.Success(UserProfile(profileEntity.userId, profileEntity.email, profileEntity.name, profileEntity.dailyGoal))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Registration failed")
        }
    }

    override suspend fun login(email: String, password: String): Resource<UserProfile> = withContext(Dispatchers.IO) {
        try {
            // 1. Authenticate directly with Firebase Auth (no remote DB hit needed for login)
            val userId = authService.login(email, password)
            val localProfile = userProfileDao.getUserProfileOnce(userId)

            val name = localProfile?.name ?: email.substringBefore("@")
            val goal = localProfile?.dailyGoal ?: 8000

            val profileEntity = UserProfileEntity(
                userId = userId,
                email = email,
                name = name,
                dailyGoal = goal
            )
            // Persist/update local Room database
            userProfileDao.upsertUserProfile(profileEntity)

            Resource.Success(UserProfile(profileEntity.userId, profileEntity.email, profileEntity.name, profileEntity.dailyGoal))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Login failed")
        }
    }


    override suspend fun continueAsGuest(): UserProfile = withContext(Dispatchers.IO) {
        val guestId = authService.enableGuestMode()
        val guestProfile = UserProfileEntity(
            userId = guestId,
            email = "guest@local",
            name = "Guest User"
        )
        userProfileDao.upsertUserProfile(guestProfile)
        UserProfile(guestId, "guest@local", "Guest User", guestProfile.dailyGoal)
    }

    override suspend fun logout(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            authService.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Logout failed")
        }
    }

    override suspend fun updateDailyGoal(newGoal: Int): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = authService.getCurrentUserId() ?: return@withContext Resource.Error("User not logged in")
            userProfileDao.updateDailyGoal(userId, newGoal)
            context?.let { TodayStepWidgetReceiver.notifyStepsUpdated(it) }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update daily goal")
        }
    }


    override suspend fun deleteAccount(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = authService.getCurrentUserId() ?: return@withContext Resource.Error("User not logged in")
            userProfileDao.deleteUserProfile(userId)
            dailyStepsDao.clearAllHistory(userId)
            authService.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete account")
        }
    }
}
