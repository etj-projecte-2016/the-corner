package com.example.thecorner.model

data class WorkoutConfig(
    val roundDurationSeconds: Int = 180,
    val restDurationSeconds: Int = 60,
    val numberOfRounds: Int = 10,
    val workoutType: WorkoutType = WorkoutType.BAG_WORK
)
