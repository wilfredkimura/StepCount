package com.example.stepcount.domain.model

/**
 * Generic result envelope for UI and domain state handling.
 * Emits Loading, Success with data, or Error with message.
 */
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}
