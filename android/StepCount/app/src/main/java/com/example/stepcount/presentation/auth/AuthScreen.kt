package com.example.stepcount.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.stepcount.R
import com.example.stepcount.core.components.AppTextField
import com.example.stepcount.core.components.PrimaryButton
import com.example.stepcount.core.components.SecondaryButton

/**
 * Authentication screen offering Login, Registration, and Guest Mode.
 * Designed with Material 3 components and vector icons.
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onAuthSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "StepCount Logo",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "StepCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (state.isLoginTab) "Welcome back! Keep moving forward." else "Create your account to start tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Tab Switcher (Login vs Register)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = if (state.isLoginTab) 0 else 1,
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ) {
                        Tab(
                            selected = state.isLoginTab,
                            onClick = { viewModel.onTabChanged(true) },
                            text = { Text("Log In", fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = !state.isLoginTab,
                            onClick = { viewModel.onTabChanged(false) },
                            text = { Text("Register", fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // General Error Banner
                if (state.generalErrorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.generalErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Name input (Only shown when registering)
                if (!state.isLoginTab) {
                    AppTextField(
                        value = state.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        label = "Display Name",
                        leadingIcon = Icons.Rounded.Person,
                        errorMessage = state.nameError
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Email input
                AppTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = "Email Address",
                    leadingIcon = Icons.Rounded.Email,
                    errorMessage = state.emailError
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password input
                AppTextField(
                    value = state.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = "Password",
                    leadingIcon = Icons.Rounded.Lock,
                    errorMessage = state.passwordError,
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(28.dp))

                // Submit Button
                PrimaryButton(
                    text = if (state.isLoading) "Please wait..." else if (state.isLoginTab) "Log In" else "Create Account",
                    onClick = { viewModel.submit() },
                    enabled = !state.isLoading,
                    leadingIcon = if (state.isLoginTab) Icons.Rounded.Login else Icons.Rounded.PersonAdd
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Continue as Guest Option
                SecondaryButton(
                    text = "Continue as Guest",
                    onClick = { viewModel.continueAsGuest() },
                    enabled = !state.isLoading,
                    leadingIcon = Icons.Rounded.SensorsOff
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
