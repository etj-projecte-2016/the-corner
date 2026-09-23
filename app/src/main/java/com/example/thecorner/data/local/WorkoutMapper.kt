package com.example.thecorner.data.local

import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutType

fun WorkoutEntity.toDomain(): Workout {
    return Workout(
        id = id,
        date = date,
        duration = duration,
        calories = calories,
        totalRounds = totalRounds,
        bagRounds = bagRounds,
        sparringRounds = sparringRounds,
        techniqueRounds = techniqueRounds,
        padRounds = padRounds,
        shadowBoxingRounds = shadowBoxingRounds,
        workoutType = WorkoutType.fromStorageId(workoutType),
        bodyWeightKgAtSession = bodyWeightKgAtSession,
        activeDurationSeconds = activeDurationSeconds,
        restDurationSeconds = restDurationSeconds
    )
}

fun Workout.toEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = id,
        date = date,
        duration = duration,
        calories = calories,
        totalRounds = totalRounds,
        bagRounds = bagRounds,
        sparringRounds = sparringRounds,
        techniqueRounds = techniqueRounds,
        padRounds = padRounds,
        shadowBoxingRounds = shadowBoxingRounds,
        workoutType = workoutType?.storageId,
        bodyWeightKgAtSession = bodyWeightKgAtSession,
        activeDurationSeconds = activeDurationSeconds,
        restDurationSeconds = restDurationSeconds
    )
}
