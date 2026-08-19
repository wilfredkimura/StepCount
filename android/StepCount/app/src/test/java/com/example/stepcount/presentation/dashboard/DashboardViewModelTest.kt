package com.example.stepcount.presentation.dashboard

import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.model.MotivationalQuote
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.model.UserProfile
import com.example.stepcount.domain.repository.AuthRepository
import com.example.stepcount.domain.usecase.GetMotivationalQuoteUseCase
import com.example.stepcount.domain.usecase.GetTodayStepsUseCase
import com.example.stepcount.domain.usecase.RecordStepDeltaUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying DashboardViewModel state flow and sensor delta recording.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getTodayStepsUseCase: GetTodayStepsUseCase
    private lateinit var recordStepDeltaUseCase: RecordStepDeltaUseCase
    private lateinit var getMotivationalQuoteUseCase: GetMotivationalQuoteUseCase
    private lateinit var getStreakUseCase: com.example.stepcount.domain.usecase.GetStreakUseCase
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getTodayStepsUseCase = mockk(relaxed = true)
        recordStepDeltaUseCase = mockk(relaxed = true)
        getMotivationalQuoteUseCase = mockk(relaxed = true)
        getStreakUseCase = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        every { authRepository.getCurrentUser() } returns flowOf(
            UserProfile("user_1", "alex@example.com", "Alex", 10000)
        )
        every { getTodayStepsUseCase.invoke() } returns flowOf(
            DailyStepRecord(1, "user_1", "2026-08-10", 6500, 10000)
        )
        coEvery { getMotivationalQuoteUseCase.invoke() } returns Resource.Success(
            MotivationalQuote("Keep walking!", "Coach")
        )
        every { getStreakUseCase.invoke() } returns flowOf(
            com.example.stepcount.domain.model.StreakInfo(5, 10, 30)
        )

        viewModel = DashboardViewModel(
            getTodayStepsUseCase,
            recordStepDeltaUseCase,
            getMotivationalQuoteUseCase,
            getStreakUseCase,
            authRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsUserGoalAndTodaySteps() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(10000, state.dailyGoal)
        assertEquals(6500L, state.liveSteps)
        assertEquals(65, state.goalPercentage)
        assertEquals(0.65f, state.progressFraction, 0.01f)
    }

    @Test
    fun onStepCountUpdatedFromSensor_triggersRecordUseCase() = runTest {
        coEvery { recordStepDeltaUseCase.invoke(7000L) } returns Resource.Success(Unit)

        viewModel.onStepCountUpdatedFromSensor(7000L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(7000L, state.liveSteps)
        coVerify { recordStepDeltaUseCase.invoke(7000L) }
    }
}
