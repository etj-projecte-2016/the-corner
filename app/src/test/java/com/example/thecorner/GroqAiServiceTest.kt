package com.example.thecorner

import com.example.thecorner.model.WorkoutAnalysis
import com.example.thecorner.ui.ai.AIError
import com.example.thecorner.ui.ai.AIResult
import com.example.thecorner.ui.ai.GroqErrorMapper
import com.example.thecorner.ui.ai.GroqAiService
import com.example.thecorner.ui.ai.GroqResponseParser
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.runBlocking

class GroqAiServiceTest {
    @Test
    fun emptyApiKeyReturnsNotConfiguredWithoutRequestingGroq() = runBlocking {
        val result = GroqAiService(apiKey = "").analyzeLastSession("ignored")

        assertEquals(AIResult.Failure(AIError.NotConfigured), result)
    }

    @Test
    fun parsesSuccessfulWorkoutAnalysisResponse() {
        val result = GroqResponseParser.parse(responseBody())

        assertEquals(
            AIResult.Success(
                WorkoutAnalysis(
                    headline = "Good structure",
                    summary = "Eight rounds were completed.",
                    positives = listOf("The planned rounds were completed."),
                    improvements = listOf("Try a varied round structure."),
                    nextSessionFocus = "Keep the round plan consistent.",
                ),
            ),
            result,
        )
    }

    @Test
    fun mapsRateLimitAndServerErrors() {
        assertEquals(AIError.Authentication, GroqErrorMapper.mapHttpStatus(401))
        assertEquals(AIError.RateLimited, GroqErrorMapper.mapHttpStatus(429))
        assertEquals(AIError.ServiceUnavailable, GroqErrorMapper.mapHttpStatus(500))
        assertEquals(AIError.ServiceUnavailable, GroqErrorMapper.mapHttpStatus(503))
    }

    @Test
    fun malformedJsonBecomesInvalidResponse() {
        assertEquals(
            AIResult.Failure(AIError.InvalidResponse),
            GroqResponseParser.parse("not-json"),
        )
    }

    private fun responseBody() = """
        {
          "choices": [
            {
              "message": {
                "content": "{\"headline\":\"Good structure\",\"summary\":\"Eight rounds were completed.\",\"positives\":[\"The planned rounds were completed.\"],\"improvements\":[\"Try a varied round structure.\"],\"nextSessionFocus\":\"Keep the round plan consistent.\"}"
              }
            }
          ]
        }
    """.trimIndent()
}
