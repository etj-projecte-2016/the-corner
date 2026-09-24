package com.example.thecorner.ui.ai

import android.os.SystemClock
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class FirebaseAiService(
    private val config: AIConfig = AIConfig(),
) : AIService {
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(config.modelName)

    override suspend fun runConnectivityTest(): AIResult<String> {
        val startedAt = SystemClock.elapsedRealtime()
        Log.d(TAG, "connectivity_test started")

        val result = AIRetryPolicy.execute(
            config = config,
            operation = {
                try {
                    val response = withTimeout(config.timeout) {
                        model.generateContent(CONNECTIVITY_PROMPT)
                    }
                    val text = response.text?.takeIf { it.isNotBlank() }
                    if (text != null) AIResult.Success(text) else AIResult.Failure(AIError.Unknown)
                } catch (timeout: TimeoutCancellationException) {
                    AIResult.Failure(AIError.Timeout)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    AIResult.Failure(AIErrorMapper.map(throwable))
                }
            },
            onRetry = { retryNumber, error ->
                Log.w(TAG, "connectivity_test retry=$retryNumber category=${error.category()}")
            },
        )

        val duration = SystemClock.elapsedRealtime() - startedAt
        when (result) {
            is AIResult.Success -> Log.d(TAG, "connectivity_test succeeded durationMs=$duration")
            is AIResult.Failure -> Log.w(
                TAG,
                "connectivity_test failed category=${result.error.category()} durationMs=$duration",
            )
        }
        return result
    }

    private companion object {
        const val TAG = "TheCornerAi"
        const val CONNECTIVITY_PROMPT = "Reply exactly with: THE CORNER AI CONNECTED"

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
