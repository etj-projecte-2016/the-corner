package com.example.thecorner

import com.example.thecorner.model.*
import com.example.thecorner.data.local.toDomain
import com.example.thecorner.data.local.toEntity
import org.junit.Assert.*
import org.junit.Test

class CaloriesCalculatorTest {
    private val tolerance = 0.000001

    private fun assertWork(type: WorkoutType, expectedMet: Double) {
        val weight = 74.5
        val expected = expectedMet * 3.5 * weight / 200 * 12.5
        assertEquals(expected, CaloriesCalculator.calculate(type, weight, 750, 0), tolerance)
    }

    @Test fun bagWorkUsesCorrectMet() = assertWork(WorkoutType.BAG_WORK, 5.8)
    @Test fun padWorkUsesCorrectMet() = assertWork(WorkoutType.PAD_WORK, 9.3)
    @Test fun shadowBoxingUsesTechniqueMet() = assertWork(WorkoutType.SHADOW_BOXING, 5.3)
    @Test fun sparringUsesCorrectMet() = assertWork(WorkoutType.SPARRING, 7.8)

    @Test fun restUsesItsOwnMetForEveryType() {
        WorkoutType.entries.forEach { type ->
            val expected = 1.5 * 3.5 * 74.5 / 200 * 9
            assertEquals(expected, CaloriesCalculator.calculate(type, 74.5, 0, 540), tolerance)
        }
    }

    @Test fun zeroDurationIsZero() {
        WorkoutType.entries.forEach { type ->
            assertEquals(0.0, CaloriesCalculator.calculate(type, 74.5, 0, 0), tolerance)
        }
    }

    @Test fun realBagExampleThirtyMinutesWorkNineMinutesRest() {
        // (5.8 * 30 + 1.5 * 9) * 3.5 * 75 / 200 = 246.09375 kcal.
        val expected = (5.8 * 30 + 1.5 * 9) * 3.5 * 75 / 200
        assertEquals(expected, CaloriesCalculator.calculate(WorkoutType.BAG_WORK, 75.0, 1800, 540), tolerance)
    }

    @Test fun completedSessionCapturesDefaultWeightAndExcludesFinalRest() {
        val workout = Workout.completed(WorkoutConfig(180, 60, 10), 123)
        assertEquals(WorkoutEnergyDefaults.DEFAULT_WEIGHT_KG, workout.bodyWeightKgAtSession!!, 0.0)
        assertEquals(1800, workout.activeDurationSeconds)
        assertEquals(540, workout.restDurationSeconds)
        assertEquals(2340, workout.duration)
        assertEquals(246, workout.calories)
        assertEquals(workout, workout.toEntity().toDomain())
    }

    @Test fun oneRoundManyRoundsAndZeroRestUseRealTrainingDurations() {
        for (rounds in listOf(1, 10, 30)) {
            for (rest in listOf(0, 60)) {
                val workout = Workout.completed(WorkoutConfig(180, rest, rounds), 123)
                assertEquals(rounds * 180, workout.activeDurationSeconds)
                assertEquals((rounds - 1) * rest, workout.restDurationSeconds)
                val expected = (5.8 * rounds * 3 + 1.5 * (rounds - 1) * rest / 60.0) * 3.5 * WorkoutEnergyDefaults.DEFAULT_WEIGHT_KG / 200
                assertEquals(expected, workout.estimatedCalories()!!, tolerance)
            }
        }
    }

    @Test fun futureWeightDoesNotChangeOlderSessionAndNullWeightUsesFallbackWithoutMutation() {
        val old = Workout.completed(WorkoutConfig(), 123)
        val newer = Workout.completed(WorkoutConfig(), 456, weightKg = 82.0)
        assertEquals(WorkoutEnergyDefaults.DEFAULT_WEIGHT_KG, old.bodyWeightKgAtSession!!, 0.0)
        assertEquals(82.0, newer.bodyWeightKgAtSession!!, 0.0)
        assertTrue(newer.estimatedCalories()!! > old.estimatedCalories()!!)
        val noWeight = old.copy(bodyWeightKgAtSession = null)
        assertEquals(old.estimatedCalories()!!, noWeight.estimatedCalories()!!, tolerance)
        assertNull(noWeight.bodyWeightKgAtSession)
    }

    @Test fun legacyDataIsNotInvented() {
        val old = Workout(1, 123, 2340, 0, 10, 10, 0, 0, workoutType = WorkoutType.BAG_WORK)
        assertNull(old.estimatedCalories())
        assertNull(old.bodyWeightKgAtSession)
        val single = old.copy(totalRounds = 1, duration = 180)
        assertEquals(5.8 * 3 * 3.5 * WorkoutEnergyDefaults.DEFAULT_WEIGHT_KG / 200,
            single.estimatedCalories()!!, tolerance)
        assertNull(single.bodyWeightKgAtSession)
        assertNull(single.copy(workoutType = null).estimatedCalories())
        assertNull(old.copy(activeDurationSeconds = 1800, restDurationSeconds = 600).estimatedCalories())
        assertNull(old.copy(bodyWeightKgAtSession = -1.0).estimatedCalories())
    }

    @Test fun invalidInputsAreRejected() {
        for (weight in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) {
                CaloriesCalculator.calculate(WorkoutType.BAG_WORK, weight, 180, 0)
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            CaloriesCalculator.calculate(WorkoutType.BAG_WORK, 74.5, -1, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            CaloriesCalculator.calculate(WorkoutType.BAG_WORK, 74.5, 180, -1)
        }
    }
}
