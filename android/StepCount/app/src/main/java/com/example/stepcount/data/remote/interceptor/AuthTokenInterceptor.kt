package com.example.stepcount.data.remote.interceptor

import com.example.stepcount.data.remote.auth.FirebaseAuthService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that automatically attaches the user's Firebase Bearer ID Token
 * to the HTTP Authorization header on all outgoing network requests.
 */
class AuthTokenInterceptor(
    private val authService: FirebaseAuthService
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // If in guest mode, proceed without adding authentication headers
        if (authService.isGuestMode()) {
            return chain.proceed(originalRequest)
        }

        // Fetch fresh Firebase ID Token synchronously for the OkHttp interceptor
        val token = runBlocking {
            authService.getFreshIdToken()
        }

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
