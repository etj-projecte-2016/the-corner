package com.example.thecorner.ui.ai

import kotlinx.coroutines.delay

object AIRetryPolicy {
    fun isRetryable(error: AIError): Boolean = when (error) {
        AIError.Timeout,
        AIError.RateLimited,
        AIError.ServiceUnavailable -> true
        AIError.Network,
        AIError.Authentication,
        AIError.Safety,
        AIError.Unknown -> false
    }

    suspend fun <T> execute(
        config: AIConfig,
        operation: suspend () -> AIResult<T>,
        onRetry: (retryNumber: Int, error: AIError) -> Unit = { _, _ -> },
    ): AIResult<T> {
        var retries = 0

        while (true) {
            when (val result = operation()) {
                is AIResult.Success -> return result
                is AIResult.Failure -> {
                    if (!isRetryable(result.error) || retries >= config.maxRetries) {
                        return result
                    }

                    retries++
                    onRetry(retries, result.error)
                    val multiplier = 1L shl (retries - 1).coerceAtMost(30)
                    delay(config.initialBackoff * multiplier)
                }
            }
        }
    }
}
