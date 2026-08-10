package com.example.stepcount.presentation.auth

import com.example.stepcount.domain.model.UserProfile
import com.example.stepcount.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests verifying AuthViewModel validation, login, and registration flows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submit_emptyEmail_showsValidationError() = runTest {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("123456")

        viewModel.submit()

        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertEquals("Email address is required", state.emailError)
        assertFalse(state.isLoading)
    }

    @Test
    fun submit_shortPassword_showsPasswordError() = runTest {
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("123")

        viewModel.submit()

        val state = viewModel.uiState.value
        assertNotNull(state.passwordError)
        assertEquals("Password must be at least 6 characters long", state.passwordError)
    }

    @Test
    fun continueAsGuest_setsSuccessState() = runTest {
        coEvery { authRepository.continueAsGuest() } returns UserProfile("guest_1", "guest@local", "Guest User")

        viewModel.continueAsGuest()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        coVerify { authRepository.continueAsGuest() }
    }
}
