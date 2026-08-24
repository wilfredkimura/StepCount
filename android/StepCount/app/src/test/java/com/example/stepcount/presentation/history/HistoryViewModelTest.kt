package com.example.stepcount.presentation.history

import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.model.StreakInfo
import com.example.stepcount.domain.usecase.DeleteStepRecordUseCase
import com.example.stepcount.domain.usecase.GetStepHistoryUseCase
import com.example.stepcount.domain.usecase.GetStreakUseCase
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
 * Unit tests verifying HistoryViewModel filtering, streaks, and deletion flows.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getStepHistoryUseCase: GetStepHistoryUseCase
    private lateinit var deleteStepRecordUseCase: DeleteStepRecordUseCase
    private lateinit var getStreakUseCase: GetStreakUseCase
    private lateinit var viewModel: HistoryViewModel

    private val mockRecords = listOf(
        DailyStepRecord(1, "user_1", "2026-08-10", 10000, 8000),
        DailyStepRecord(2, "user_1", "2026-08-09", 8500, 8000),
        DailyStepRecord(3, "user_1", "2026-08-08", 4000, 8000)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getStepHistoryUseCase = mockk(relaxed = true)
        deleteStepRecordUseCase = mockk(relaxed = true)
        getStreakUseCase = mockk(relaxed = true)

        every { getStepHistoryUseCase.invoke("all") } returns flowOf(mockRecords)
        every { getStreakUseCase.invoke() } returns flowOf(StreakInfo(currentStreak = 2, bestStreak = 5, totalGoalDays = 12))

        viewModel = HistoryViewModel(getStepHistoryUseCase, deleteStepRecordUseCase, getStreakUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadHistory_computesTotalStepsCorrectly() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.records.size)
        assertEquals(22500L, state.totalHistorySteps)
    }

    @Test
    fun deleteRecord_executesUseCase() = runTest {
        val recordToDelete = mockRecords[0]
        coEvery { deleteStepRecordUseCase.invoke("2026-08-10") } returns Resource.Success(Unit)

        viewModel.promptDeleteRecord(recordToDelete)
        assertEquals(recordToDelete, viewModel.uiState.value.recordPendingDeletion)

        viewModel.confirmDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteStepRecordUseCase.invoke("2026-08-10") }
        assertNull(viewModel.uiState.value.recordPendingDeletion)
    }
}
