package com.example.thecorner.data.local

import com.example.thecorner.model.Workout

fun WorkoutEntity.toDomain(): Workout {
    return Workout(
        id = id,
        date = date,
        duration = duration,
        calories = calories,
        totalRounds = totalRounds,
        bagRounds = bagRounds,
        sparringRounds = sparringRounds,
        techniqueRounds = techniqueRounds
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
        techniqueRounds = techniqueRounds
    )
}