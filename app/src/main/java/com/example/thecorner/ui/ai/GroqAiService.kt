package com.example.thecorner.ui.ai

import android.os.SystemClock
import android.util.Log
import com.example.thecorner.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeoutException

class GroqAiService(
    private val config: AIConfig = AIConfig(),
    private val apiKey: String = BuildConfig.GROQ_API_KEY,
) : AIService {
    override val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    override suspend fun analyzeLastSession(prompt: String): AIResult<com.example.thecorner.model.WorkoutAnalysis> {
        if (!isConfigured) return AIResult.Failure(AIError.NotConfigured)

        val startedAt = SystemClock.elapsedRealtime()
        log("operation=analyze_last_session started")

        val result = AIRetryPolicy.execute(
            config = config,
            operation = { request(prompt) },
            onRetry = { retryNumber, error ->
                log("operation=analyze_last_session retry=$retryNumber category=${error.category()}")
            },
        )

        when (result) {
            is AIResult.Success -> log(
                "operation=analyze_last_session succeeded durationMs=${SystemClock.elapsedRealtime() - startedAt}",
            )
            is AIResult.Failure -> log(
                "operation=analyze_last_session failed category=${result.error.category()} " +
                    "durationMs=${SystemClock.elapsedRealtime() - startedAt}",
            )
        }
        return result
    }

    private suspend fun request(prompt: String): AIResult<com.example.thecorner.model.WorkoutAnalysis> =
        try {
            withTimeout(config.timeout) {
                withContext(Dispatchers.IO) {
                    val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = config.timeout.toInt()
                        readTimeout = config.timeout.toInt()
                        doOutput = true
                        setRequestProperty("Authorization", "Bearer $apiKey")
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Accept", "application/json")
                    }

                    try {
                        connection.outputStream.use { output ->
                            output.write(requestBody(prompt).toString().toByteArray(StandardCharsets.UTF_8))
                        }
                        val statusCode = connection.responseCode
                        val responseBody = (if (statusCode in 200..299) {
                            connection.inputStream
                        } else {
                            connection.errorStream
                        })?.bufferedReader()?.use { it.readText() }.orEmpty()

                        if (statusCode !in 200..299) {
                            AIResult.Failure(GroqErrorMapper.mapHttpStatus(statusCode))
                        } else {
                            GroqResponseParser.parse(responseBody)
                        }
                    } finally {
                        connection.disconnect()
                    }
                }
            }
        } catch (timeout: TimeoutCancellationException) {
            AIResult.Failure(AIError.Timeout)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (timeout: SocketTimeoutException) {
            AIResult.Failure(AIError.Timeout)
        } catch (timeout: TimeoutException) {
            AIResult.Failure(AIError.Timeout)
        } catch (network: IOException) {
            AIResult.Failure(AIError.Network)
        } catch (throwable: Throwable) {
            AIResult.Failure(GroqErrorMapper.map(throwable))
        }

    private fun requestBody(prompt: String): JsonObject = buildJsonObject {
        put("model", MODEL)
        put("messages", buildJsonArray {
            add(buildJsonObject {
                put("role", "user")
                put("content", prompt)
            })
        })
        put("response_format", buildJsonObject {
            put("type", "json_schema")
            put("json_schema", buildJsonObject {
                put("name", "workout_analysis")
                put("strict", true)
                put("schema", workoutAnalysisJsonSchema())
            })
        })
    }

    private fun workoutAnalysisJsonSchema(): JsonObject = buildJsonObject {
        put("type", "object")
        put("properties", buildJsonObject {
            put("headline", buildJsonObject { put("type", "string") })
            put("summary", buildJsonObject { put("type", "string") })
            put("positives", buildJsonObject {
                put("type", "array")
                put("items", buildJsonObject { put("type", "string") })
            })
            put("improvements", buildJsonObject {
                put("type", "array")
                put("items", buildJsonObject { put("type", "string") })
            })
            put("nextSessionFocus", buildJsonObject { put("type", "string") })
        })
        put("required", buildJsonArray {
            add(JsonPrimitive("headline"))
            add(JsonPrimitive("summary"))
            add(JsonPrimitive("positives"))
            add(JsonPrimitive("improvements"))
            add(JsonPrimitive("nextSessionFocus"))
        })
        put("additionalProperties", false)
    }

    private fun log(message: String) {
        Log.d(TAG, "provider=groq model=$MODEL $message")
    }

    private companion object {
        const val TAG = "TheCornerAi"
        const val MODEL = "openai/gpt-oss-20b"
        const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"

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

object GroqResponseParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(responseBody: String): AIResult<com.example.thecorner.model.WorkoutAnalysis> = runCatching {
        val content = json.parseToJsonElement(responseBody).jsonObject
            .getValue("choices").jsonArray.first().jsonObject
            .getValue("message").jsonObject.getValue("content").jsonPrimitive.content
        json.parseToJsonElement(content).jsonObject
    }.fold(
        onSuccess = { WorkoutAnalysisParser.parse(it) },
        onFailure = { AIResult.Failure(AIError.InvalidResponse) },
    )
}

object GroqErrorMapper {
    fun mapHttpStatus(statusCode: Int): AIError = when {
        statusCode == 401 || statusCode == 403 -> AIError.Authentication
        statusCode == 429 -> AIError.RateLimited
        statusCode in 500..599 -> AIError.ServiceUnavailable
        else -> AIError.Unknown
    }

    fun map(throwable: Throwable): AIError = when (throwable) {
        is SocketTimeoutException, is TimeoutException -> AIError.Timeout
        is IOException -> AIError.Network
        else -> AIError.InvalidResponse
    }
}
