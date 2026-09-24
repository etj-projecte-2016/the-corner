package com.example.thecorner.ui.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Success(val text: String) : AiUiState
    data class Error(val error: AIError) : AiUiState
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
    private val requestScope: CoroutineScope? = null,
    private val logger: AiLogger = AndroidAiLogger,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    fun runConnectivityTest() {
        if (_uiState.value is AiUiState.Loading) return

        (requestScope ?: viewModelScope).launch {
            _uiState.value = AiUiState.Loading
            val startedAt = System.nanoTime()
            when (val result = service.runConnectivityTest()) {
                is AIResult.Success -> {
                    logger.debug("connectivity_test succeeded durationMs=${elapsedSince(startedAt)}")
                    _uiState.value = AiUiState.Success(result.data)
                }

                is AIResult.Failure -> {
                    logger.warning("connectivity_test failed category=${result.error.category()}")
                    _uiState.value = AiUiState.Error(result.error)
                }
            }
        }
    }

    private companion object {
        fun elapsedSince(startedAt: Long): Long = (System.nanoTime() - startedAt) / 1_000_000L

        fun AIError.category(): String = when (this) {
            AIError.Network -> "network"
            AIError.Timeout -> "timeout"
            AIError.RateLimited -> "rate_limited"
            AIError.ServiceUnavailable -> "service_unavailable"
            AIError.Authentication -> "authentication"
            AIError.Safety -> "safety"
            AIError.Unknown -> "unknown"
        }
    }
}
