package com.example.thecorner.ui.ai

import kotlinx.coroutines.delay

object AIRetryPolicy {
    private val retryDelays = longArrayOf(2_000L, 5_000L)

    fun isRetryable(error: AIError): Boolean = when (error) {
        AIError.Timeout,
        AIError.Network,
        AIError.ServiceUnavailable -> true
        AIError.RateLimited,
        AIError.Authentication,
        AIError.NotConfigured,
        AIError.Safety,
        AIError.NoWorkout,
        AIError.InvalidResponse,
        AIError.Unknown -> false
    }

    suspend fun <T> execute(
        config: AIConfig,
        operation: suspend () -> AIResult<T>,
        onRetry: (retryNumber: Int, error: AIError) -> Unit = { _, _ -> },
        wait: suspend (delayMs: Long) -> Unit = { delay(it) },
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
                    wait(retryDelays[retries - 1])
                }
            }
        }
    }
}
