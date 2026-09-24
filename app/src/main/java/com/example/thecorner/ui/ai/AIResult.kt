package com.example.thecorner.ui.ai

sealed interface AIResult<out T> {
    data class Success<T>(val data: T) : AIResult<T>

    data class Failure(val error: AIError) : AIResult<Nothing>
}

sealed interface AIError {
    data object Network : AIError
    data object Timeout : AIError
    data object RateLimited : AIError
    data object ServiceUnavailable : AIError
    data object Authentication : AIError
    data object Safety : AIError
    data object Unknown : AIError
}
