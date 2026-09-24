package com.example.thecorner.ui.ai

import com.example.thecorner.model.Workout

object AiPromptBuilder {
    fun build(latest: Workout, recent: List<Workout>): String {
        fun describe(workout: Workout): String = buildString {
            append("Type: ").append(workout.workoutType?.name ?: "UNKNOWN")
            append(", Duration: ").append(workout.duration / 60).append(" minutes")
            append(", Rounds: ").append(workout.totalRounds)
            workout.activeDurationSeconds?.let { append(", Active: ").append(it / 60).append(" minutes") }
            workout.restDurationSeconds?.let { append(", Rest: ").append(it / 60).append(" minutes") }
            append(", Calories: ").append(workout.calories)
        }

        return """
            You are analyzing boxing training data.
            Only make conclusions supported by the supplied data. Do not invent technique,
            performance, medical or physiological information. Keep the response short.

            ## Latest session
            ${describe(latest)}

            ## Recent sessions
            ${recent.joinToString("\n") { "- ${describe(it)}" }}

            Give a short analysis of the latest session compared with the recent training history.
        """.trimIndent()
    }
}
