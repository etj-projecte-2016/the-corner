package com.example.thecorner

import com.example.thecorner.model.WorkoutAnalysis
import com.example.thecorner.ui.ai.AIError
import com.example.thecorner.ui.ai.AIResult
import com.example.thecorner.ui.ai.WorkoutAnalysisParser
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutAnalysisParserTest {
    @Test
    fun parsesStructuredResponseIntoDomainModel() {
        val result = WorkoutAnalysisParser.parse(validJson())

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
    fun malformedStructuredResponseBecomesInvalidResponse() {
        val result = WorkoutAnalysisParser.parse(
            buildJsonObject {
                put("headline", "Missing required fields")
            },
        )

        assertEquals(AIResult.Failure(AIError.InvalidResponse), result)
    }

    private fun validJson() = buildJsonObject {
        put("headline", "Good structure")
        put("summary", "Eight rounds were completed.")
        put("positives", buildJsonArray { add(JsonPrimitive("The planned rounds were completed.")) })
        put("improvements", buildJsonArray { add(JsonPrimitive("Try a varied round structure.")) })
        put("nextSessionFocus", "Keep the round plan consistent.")
    }
}
