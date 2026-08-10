package com.example.stepcount.data.remote.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.stepcount.core.util.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Service managing user authentication with Firebase Auth and offline Guest Mode.
 * Handles user registration, login, token retrieval, and guest session flags.
 */
class FirebaseAuthService(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Checks if the user is currently using the app in Guest Mode.
     */
    fun isGuestMode(): Boolean {
        return prefs.getBoolean(Constants.KEY_GUEST_MODE, false)
    }

    /**
     * Enables Guest Mode by creating and storing a local guest ID.
     */
    fun enableGuestMode(): String {
        var guestId = prefs.getString("guest_user_id", null)
        if (guestId == null) {
            guestId = "guest_" + UUID.randomUUID().toString().substring(0, 8)
            prefs.edit()
                .putString("guest_user_id", guestId)
                .putBoolean(Constants.KEY_GUEST_MODE, true)
                .apply()
        } else {
            prefs.edit().putBoolean(Constants.KEY_GUEST_MODE, true).apply()
        }
        return guestId
    }

    /**
     * Disables Guest Mode flag.
     */
    fun disableGuestMode() {
        prefs.edit().putBoolean(Constants.KEY_GUEST_MODE, false).apply()
    }

    /**
     * Returns the current Firebase user ID or guest user ID.
     */
    fun getCurrentUserId(): String? {
        if (isGuestMode()) {
            return prefs.getString("guest_user_id", "guest_default")
        }
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Returns the current logged-in user email or "guest@local".
     */
    fun getCurrentUserEmail(): String? {
        if (isGuestMode()) return "guest@local"
        return firebaseAuth.currentUser?.email
    }

    /**
     * Retrieves a fresh Firebase ID Token for API requests.
     * Returns null if in guest mode or not logged in.
     */
    suspend fun getFreshIdToken(): String? {
        if (isGuestMode()) return null
        val user = firebaseAuth.currentUser ?: return null
        return try {
            val tokenResult = user.getIdToken(false).await()
            tokenResult.token
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Registers a new account with email and password in Firebase Auth.
     */
    suspend fun register(email: String, password: String): String {
        disableGuestMode()
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("Failed to get user ID after registration")
    }

    /**
     * Authenticates with email and password in Firebase Auth.
     */
    suspend fun login(email: String, password: String): String {
        disableGuestMode()
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("Failed to get user ID after login")
    }

    /**
     * Signs out the user from Firebase and clears guest mode.
     */
    fun signOut() {
        disableGuestMode()
        firebaseAuth.signOut()
    }
}
