package com.example.stepcount.core.util

import com.example.stepcount.BuildConfig

/**
 * Supported backend hosting destinations for the StepCount application.
 * You can set these in 'local.properties' (e.g. backend.url=http://10.0.2.2:8000/api/)
 * or override dynamically at runtime for debugging.
 */
enum class BackendEnvironment(
    val baseUrl: String,
    val description: String
) {
    /**
     * Use when running the app inside an Android Studio Emulator while FastAPI runs on your PC.
     * Android Emulator routes '10.0.2.2' directly to the host PC's 'localhost:8000'.
     */
    LOCAL_EMULATOR(
        baseUrl = "http://10.0.2.2:8000/api/",
        description = "Local PC via Android Emulator (10.0.2.2:8000)"
    ),

    /**
     * Use when running the app on a physical Android device over the same Wi-Fi network.
     * Replace with your PC's local Wi-Fi IP address (found via 'ipconfig').
     */
    LOCAL_DEVICE(
        baseUrl = "http://192.168.1.100:8000/api/",
        description = "Local PC via Wi-Fi Physical Device"
    ),

    /**
     * Production cloud backend hosted on Render.
     * Replace with your live Render Web Service URL once deployed.
     */
    RENDER_PRODUCTION(
        baseUrl = "https://stepcount-backend.onrender.com/api/",
        description = "Render Cloud Production (HTTPS)"
    )
}

/**
 * Central Configuration file for StepCount.
 * Base URL is automatically loaded from local.properties / BuildConfig.BASE_URL at build time.
 */
object AppConfig {

    /**
     * Optional runtime override. If null, the app uses BuildConfig.BASE_URL loaded from local.properties.
     */
    var customOverrideUrl: String? = null

    /**
     * Returns the active base URL for Retrofit API communication.
     * Prioritizes any runtime override, otherwise uses BuildConfig.BASE_URL from local.properties.
     */
    val BASE_URL: String
        get() = customOverrideUrl ?: BuildConfig.BASE_URL
}

