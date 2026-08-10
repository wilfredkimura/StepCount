package com.example.stepcount.data.remote.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.stepcount.core.util.Constants
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Service managing user authentication with Firebase Auth and offline Guest Mode.
 * Handles user registration, login, token retrieval, and guest session flags.
 * Includes defensive fallback so the app starts smoothly even before Firebase is configured.
 */
class FirebaseAuthService(
    private val context: Context,
    customFirebaseAuth: FirebaseAuth? = null
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val firebaseAuth: FirebaseAuth? = customFirebaseAuth ?: run {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            // FirebaseApp not configured with google-services.json yet; app runs in offline/guest mode
            null
        }
    }

    /**
     * Checks if the user is currently using the app in Guest Mode.
     */
    fun isGuestMode(): Boolean {
        return prefs.getBoolean(Constants.KEY_GUEST_MODE, false) || firebaseAuth == null
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
        return firebaseAuth?.currentUser?.uid ?: prefs.getString("guest_user_id", "guest_default")
    }

    /**
     * Returns the current logged-in user email or "guest@local".
     */
    fun getCurrentUserEmail(): String? {
        if (isGuestMode()) return "guest@local"
        return firebaseAuth?.currentUser?.email ?: "guest@local"
    }

    /**
     * Retrieves a fresh Firebase ID Token for API requests.
     * Returns null if in guest mode or not logged in.
     */
    suspend fun getFreshIdToken(): String? {
        if (isGuestMode()) return null
        val user = firebaseAuth?.currentUser ?: return null
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
        val auth = firebaseAuth ?: throw IllegalStateException(
            "Firebase is not configured yet. Please use 'Continue as Guest' or add google-services.json."
        )
        disableGuestMode()
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("Failed to get user ID after registration")
    }

    /**
     * Authenticates with email and password in Firebase Auth.
     */
    suspend fun login(email: String, password: String): String {
        val auth = firebaseAuth ?: throw IllegalStateException(
            "Firebase is not configured yet. Please use 'Continue as Guest' or add google-services.json."
        )
        disableGuestMode()
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("Failed to get user ID after login")
    }

    /**
     * Signs out the user from Firebase and clears guest mode.
     */
    fun signOut() {
        disableGuestMode()
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            // Ignore sign-out errors when offline
        }
    }
}
