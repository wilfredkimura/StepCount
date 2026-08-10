package com.example.stepcount.presentation.auth

/**
 * UI State for the Login and Registration screens.
 */
data class AuthUiState(
    val isLoginTab: Boolean = true,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val generalErrorMessage: String? = null,
    val isSuccess: Boolean = false
)
