package com.example.stepcount.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing Login and Registration logic with strict input validation.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onTabChanged(isLogin: Boolean) {
        _uiState.update {
            it.copy(
                isLoginTab = isLogin,
                emailError = null,
                passwordError = null,
                nameError = null,
                generalErrorMessage = null
            )
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, generalErrorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, generalErrorMessage = null) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null, generalErrorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val state = _uiState.value

        // Validate Email
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email address is required") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address (e.g. user@example.com)") }
            isValid = false
        }

        // Validate Password
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters long") }
            isValid = false
        }

        // Validate Display Name if in Registration tab
        if (!state.isLoginTab && state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Display name is required") }
            isValid = false
        }

        return isValid
    }

    fun submit() {
        if (!validateInputs()) return

        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, generalErrorMessage = null) }

        viewModelScope.launch {
            val result = if (state.isLoginTab) {
                authRepository.login(state.email.trim(), state.password)
            } else {
                authRepository.register(state.email.trim(), state.password, state.name.trim())
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, generalErrorMessage = result.message) }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun continueAsGuest() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.continueAsGuest()
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
