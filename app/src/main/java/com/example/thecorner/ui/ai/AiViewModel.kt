package com.example.thecorner.ui.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thecorner.data.repository.WorkoutRepository
import com.example.thecorner.model.WorkoutAnalysis
import com.example.thecorner.model.Workout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AiUiState {
    data object Idle : AiUiState
    data object NotConfigured : AiUiState
    data class Ready(val workout: Workout) : AiUiState
    data class Loading(val workout: Workout) : AiUiState
    data class AnalysisSuccess(val analysis: WorkoutAnalysis, val workout: Workout) : AiUiState
    data object NoWorkout : AiUiState
    data class Error(val error: AIError, val workout: Workout? = null) : AiUiState
}

interface AiLogger {
    fun debug(message: String)
    fun warning(message: String)
}

object AndroidAiLogger : AiLogger {
    override fun debug(message: String) {
        Log.d(TAG, message)
    }

    override fun warning(message: String) {
        Log.w(TAG, message)
    }

    private const val TAG = "TheCornerAi"
}

class AiViewModel(
    private val service: AIService,
    private val workoutRepository: WorkoutRepository,
    private val requestScope: CoroutineScope? = null,
    private val logger: AiLogger = AndroidAiLogger,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    fun analyzeLastSession() {
        if (_uiState.value is AiUiState.Loading) return
        if (!service.isConfigured) {
            _uiState.value = AiUiState.NotConfigured
            return
        }

        (requestScope ?: viewModelScope).launch {
            val startedAt = System.nanoTime()
            try {
                val latestWorkout = workoutRepository.getLastWorkout().first()
                if (latestWorkout == null) {
                    _uiState.value = AiUiState.NoWorkout
                    return@launch
                }

                _uiState.value = AiUiState.Loading(latestWorkout)
                val prompt = AiPromptBuilder.buildAnalysisPrompt(latestWorkout)
                when (val result = service.analyzeLastSession(prompt)) {
                    is AIResult.Success -> {
                        logger.debug(
                            "analyze_last_session succeeded durationMs=${elapsedSince(startedAt)}",
                        )
                        _uiState.value = AiUiState.AnalysisSuccess(result.data, latestWorkout)
                    }

                    is AIResult.Failure -> publishFailure("analyze_last_session", result.error, latestWorkout)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                publishFailure(
                    "analyze_last_session",
                    AIErrorMapper.map(throwable),
                    (_uiState.value as? AiUiState.Loading)?.workout,
                )
            }
        }
    }

    /**
     * Called when the AI destination becomes visible again. The existing success state is
     * session-scoped, but it must not be shown for a newer latest workout.
     */
    fun onScreenVisible() {
        (requestScope ?: viewModelScope).launch {
            if (!service.isConfigured) {
                _uiState.value = AiUiState.NotConfigured
                return@launch
            }

            val latestWorkout = workoutRepository.getLastWorkout().first()
            when (val currentState = _uiState.value) {
                AiUiState.Idle,
                AiUiState.NoWorkout,
                -> _uiState.value = latestWorkout?.let(AiUiState::Ready) ?: AiUiState.NoWorkout
                AiUiState.NotConfigured -> Unit
                is AiUiState.AnalysisSuccess -> if (latestWorkout?.id != currentState.workout.id) {
                    _uiState.value = latestWorkout?.let(AiUiState::Ready) ?: AiUiState.NoWorkout
                }
                is AiUiState.Ready -> if (latestWorkout?.id != currentState.workout.id) {
                    _uiState.value = latestWorkout?.let(AiUiState::Ready) ?: AiUiState.NoWorkout
                }
                is AiUiState.Error -> if (latestWorkout?.id != currentState.workout?.id) {
                    _uiState.value = latestWorkout?.let(AiUiState::Ready) ?: AiUiState.NoWorkout
                }
                is AiUiState.Loading -> Unit
            }
        }
    }

    private fun publishFailure(operation: String, error: AIError, workout: Workout? = null) {
        logger.warning("$operation failed category=${error.category()}")
        _uiState.value = AiUiState.Error(error, workout)
    }

    private companion object {
        fun elapsedSince(startedAt: Long): Long = (System.nanoTime() - startedAt) / 1_000_000L

        fun AIError.category(): String = when (this) {
            AIError.NotConfigured -> "not_configured"
            AIError.Network -> "network"
            AIError.Timeout -> "timeout"
            AIError.RateLimited -> "rate_limited"
            AIError.ServiceUnavailable -> "service_unavailable"
            AIError.Authentication -> "authentication"
            AIError.Safety -> "safety"
            AIError.NoWorkout -> "no_workout"
            AIError.InvalidResponse -> "invalid_response"
            AIError.Unknown -> "unknown"
        }
    }
}
