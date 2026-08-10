package com.example.stepcount.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.usecase.DeleteStepRecordUseCase
import com.example.stepcount.domain.usecase.GetStepHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing historical step logs, filtering by timeframe, and deleting records.
 */
class HistoryViewModel(
    private val getStepHistoryUseCase: GetStepHistoryUseCase,
    private val deleteStepRecordUseCase: DeleteStepRecordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory("all")
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(activeFilter = filter) }
        loadHistory(filter)
    }

    private fun loadHistory(filter: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getStepHistoryUseCase(filter).collect { list ->
                val total = list.sumOf { it.steps }
                _uiState.update {
                    it.copy(
                        records = list,
                        totalHistorySteps = total,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun promptDeleteRecord(record: DailyStepRecord) {
        _uiState.update { it.copy(recordPendingDeletion = record) }
    }

    fun dismissDeletePrompt() {
        _uiState.update { it.copy(recordPendingDeletion = null) }
    }

    fun confirmDelete() {
        val record = _uiState.value.recordPendingDeletion ?: return
        viewModelScope.launch {
            deleteStepRecordUseCase(record.date)
            _uiState.update { it.copy(recordPendingDeletion = null) }
        }
    }
}
