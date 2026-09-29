package com.example.thecorner

import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.ai.AiPromptBuilder
import org.junit.Assert.assertTrue
import org.junit.Test

class AiPromptBuilderTest {
    @Test
    fun analysisPromptSetsBoxingCoachRulesAndUsesDerivedStructure() {
        val workout = Workout.completed(WorkoutConfig(180, 60, 8, WorkoutType.BAG_WORK), 100L)

        val prompt = AiPromptBuilder.buildAnalysisPrompt(workout)

        assertTrue(prompt.contains("Type: bag work"))
        assertTrue(prompt.contains("Rounds: 8"))
        assertTrue(prompt.contains("Interpret what the workout structure means instead of repeating statistics"))
        assertTrue(prompt.contains("Never invent or claim"))
        assertTrue(prompt.contains("punch count, speed, accuracy, power"))
        assertTrue(prompt.contains("Bag work: conditioning, purposeful combination structure"))
        assertTrue(prompt.contains("nextSessionFocus: make this the most actionable field"))
        assertTrue(prompt.contains("Work-to-rest ratio:"))
        assertTrue(!prompt.contains("weight"))
    }

    @Test
    fun promptDistinguishesWorkoutTypesAndKeepsSafetyRules() {
        val workout = Workout.completed(WorkoutConfig(180, 60, 8, WorkoutType.BAG_WORK), 100L)

        val prompt = AiPromptBuilder.buildAnalysisPrompt(workout)

        assertTrue(prompt.contains("Type: bag work"))
        assertTrue(prompt.contains("Rounds: 8"))
        assertTrue(prompt.contains("technique quality"))
        assertTrue(prompt.contains("Do not claim"))
        assertTrue(prompt.contains("that a boxer improved or performed well"))
        assertTrue(prompt.contains("Pad work: timing, accuracy, reactions"))
        assertTrue(prompt.contains("Sparring: tactical development, distance management"))
        assertTrue(prompt.contains("Shadow boxing: movement, technique rehearsal"))
    }
}
