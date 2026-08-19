package com.example.thecorner.ui.training

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkoutSetupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        WorkoutSetupUiState()
    )

    val uiState: StateFlow<WorkoutSetupUiState> =
        _uiState.asStateFlow()

    fun increaseRoundDuration() {
        _uiState.update { currentState ->
            currentState.copy(
                roundDuration = currentState.roundDuration + 30
            )
        }
    }

    fun decreaseRoundDuration() {
        _uiState.update { currentState ->
            currentState.copy(
                roundDuration = (currentState.roundDuration - 30)
                    .coerceAtLeast(30)
            )
        }
    }

    fun increaseRestDuration() {
        _uiState.update { currentState ->
            currentState.copy(
                restDuration = currentState.restDuration + 15
            )
        }
    }

    fun decreaseRestDuration() {
        _uiState.update { currentState ->
            currentState.copy(
                restDuration = (currentState.restDuration - 15)
                    .coerceAtLeast(0)
            )
        }
    }

    fun increaseRounds() {
        _uiState.update { currentState ->
            currentState.copy(
                rounds = currentState.rounds + 1
            )
        }
    }

    fun decreaseRounds() {
        _uiState.update { currentState ->
            currentState.copy(
                rounds = (currentState.rounds - 1)
                    .coerceAtLeast(1)
            )
        }
    }
}