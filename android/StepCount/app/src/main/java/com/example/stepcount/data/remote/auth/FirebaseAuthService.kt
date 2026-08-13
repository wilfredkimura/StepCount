package com.example.stepcount.data.remote.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.stepcount.core.util.Constants
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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
                try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    // Fallback to explicit options matching google-services.json
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:927399598438:android:69965c728fc50e19d9d0a7")
                        .setApiKey("AIzaSyCf0uzRU95GV52rHZZEa5j50PN4s5gLN44")
                        .setProjectId("stepcount-5c9f9")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                }
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            // App runs in offline/guest mode if Firebase is completely unreachable
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
     * Registers a new account with email, password, and display name in Firebase Auth.
     */
    suspend fun register(email: String, password: String, name: String = ""): String {
        val auth = firebaseAuth ?: throw IllegalStateException(
            "Firebase is not configured yet. Please use 'Continue as Guest' or add google-services.json."
        )
        disableGuestMode()
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Failed to get user ID after registration")

        // Set user's display name so token claims contain the name immediately
        if (name.isNotBlank()) {
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                // Ignore non-fatal display name update error during offline registration
            }
        }

        return user.uid
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

