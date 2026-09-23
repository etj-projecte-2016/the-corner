package com.example.thecorner

import com.example.thecorner.data.local.toDomain
import com.example.thecorner.data.local.toEntity
import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import org.junit.Assert.*
import org.junit.Test

class WorkoutTypeTest {
    @Test fun completedSessionsClassifyEveryRoundAndRoundTrip() {
        val expected = mapOf(
            WorkoutType.BAG_WORK to listOf(6, 0, 0, 0),
            WorkoutType.PAD_WORK to listOf(0, 6, 0, 0),
            WorkoutType.SPARRING to listOf(0, 0, 6, 0),
            WorkoutType.SHADOW_BOXING to listOf(0, 0, 0, 6)
        )
        expected.forEach { (type, counts) ->
            val workout = Workout.completed(WorkoutConfig(180, 60, 6, type), 123L)
            assertEquals(counts, listOf(workout.bagRounds, workout.padRounds,
                workout.sparringRounds, workout.shadowBoxingRounds))
            assertEquals(0, workout.techniqueRounds)
            assertEquals(6, workout.totalRounds)
            assertEquals(1380, workout.duration)
            assertEquals(type, workout.workoutType)
            assertEquals(workout, workout.toEntity().toDomain())
        }
    }

    @Test fun historicalTechniqueRemainsUnknown() {
        val historical = Workout(1, 123L, 900, 0, 5, 0, 0, 5)
        assertEquals(historical, historical.toEntity().toDomain())
        assertNull(historical.workoutType)
        assertEquals(0, historical.padRounds)
        assertEquals(0, historical.shadowBoxingRounds)
        assertNull(WorkoutType.fromStorageId("technique"))
        assertNull(WorkoutType.fromStorageId(null))
    }
}
