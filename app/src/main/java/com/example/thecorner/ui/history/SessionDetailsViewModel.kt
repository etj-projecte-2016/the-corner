package com.example.thecorner.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thecorner.data.repository.WorkoutRepository
import com.example.thecorner.model.Workout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class SessionDetailsUiState(
    val session: Workout? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class SessionDetailsViewModel(repository: WorkoutRepository, sessionId: Long) : ViewModel() {
    private val refresh = MutableStateFlow(0)
    val uiState: StateFlow<SessionDetailsUiState> = refresh.flatMapLatest {
        repository.getWorkoutById(sessionId)
            .map { SessionDetailsUiState(session = it, isLoading = false) }
            .onStart { emit(SessionDetailsUiState()) }
            .catch { emit(SessionDetailsUiState(isLoading = false, hasError = true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionDetailsUiState())

    fun retry() { refresh.value += 1 }
}
