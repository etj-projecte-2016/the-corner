package com.example.thecorner.ui.ai

import com.example.thecorner.model.WorkoutAnalysis
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

object WorkoutAnalysisParser {
    fun parse(json: JsonObject?): AIResult<WorkoutAnalysis> = runCatching {
        requireNotNull(json)
        WorkoutAnalysis(
            headline = json.requiredString("headline"),
            summary = json.requiredString("summary"),
            positives = json.requiredStringList("positives"),
            improvements = json.requiredStringList("improvements"),
            nextSessionFocus = json.requiredString("nextSessionFocus"),
        )
    }.fold(
        onSuccess = { AIResult.Success(it) },
        onFailure = { AIResult.Failure(AIError.InvalidResponse) },
    )

    private fun JsonObject.requiredString(name: String): String =
        (this[name] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: error("Missing or invalid field: $name")

    private fun JsonObject.requiredStringList(name: String): List<String> =
        (this[name] as? JsonArray)?.map { element ->
            (element as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
                ?: error("Invalid list item in field: $name")
        } ?: error("Missing or invalid field: $name")
}
