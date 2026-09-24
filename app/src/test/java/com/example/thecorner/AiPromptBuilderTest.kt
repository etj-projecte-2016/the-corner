package com.example.thecorner

import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.ai.AiPromptBuilder
import org.junit.Assert.assertTrue
import org.junit.Test

class AiPromptBuilderTest {
    @Test
    fun promptContainsOnlySuppliedSessionFacts() {
        val latest = Workout.completed(WorkoutConfig(180, 60, 8, WorkoutType.BAG_WORK), 100L)
        val previous = Workout.completed(WorkoutConfig(120, 30, 4, WorkoutType.PAD_WORK), 200L)

        val prompt = AiPromptBuilder.build(latest, listOf(previous))

        assertTrue(prompt.contains("Type: BAG_WORK"))
        assertTrue(prompt.contains("Rounds: 8"))
        assertTrue(prompt.contains("Type: PAD_WORK"))
        assertTrue(prompt.contains("Do not invent technique"))
    }
}
