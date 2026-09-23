package com.example.thecorner.ui.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thecorner.data.repository.WorkoutRepository
import com.example.thecorner.model.WorkoutType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    repository: WorkoutRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val refresh = MutableStateFlow(0)
    private val filter = savedState.getStateFlow<String?>(FILTER, null)

    val uiState: StateFlow<HistoryUiState> = combine(filter, refresh) { value, _ ->
        WorkoutType.fromStorageId(value)
    }.flatMapLatest { selected ->
        repository.getAllWorkouts()
            .map {
                val months = historyMonths(it, selected)
                HistoryUiState(months, selected, isLoading = false,
                    summary = historySummary(months.flatMap { month -> month.sessions }))
            }
            .onStart { emit(HistoryUiState(selectedFilter = selected)) }
            .catch { emit(HistoryUiState(selectedFilter = selected, isLoading = false, hasError = true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState(
        selectedFilter = WorkoutType.fromStorageId(savedState[FILTER])
    ))

    fun selectFilter(type: WorkoutType?) { savedState[FILTER] = type?.storageId }
    fun retry() { refresh.value += 1 }

    private companion object { const val FILTER = "history_filter" }
}
