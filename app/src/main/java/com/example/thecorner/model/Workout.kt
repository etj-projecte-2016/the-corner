package com.example.thecorner.model

data class Workout (

    val id: Long,
    val date: Long,
    // Configured rounds plus rests between rounds, in seconds; excludes pauses and preparation.
    val duration: Int,
    val calories: Int,
    val totalRounds: Int,
    // Historical records may contain technique or unclassified rounds.
    val bagRounds: Int,
    val sparringRounds: Int,
    val techniqueRounds: Int,
    val padRounds: Int = 0,
    val shadowBoxingRounds: Int = 0,
    val workoutType: WorkoutType? = null

) {
    companion object {
        fun completed(config: WorkoutConfig, date: Long): Workout = Workout(
            id = 0,
            date = date,
            duration = config.numberOfRounds * config.roundDurationSeconds +
                (config.numberOfRounds - 1).coerceAtLeast(0) * config.restDurationSeconds,
            calories = 0,
            totalRounds = config.numberOfRounds,
            bagRounds = if (config.workoutType == WorkoutType.BAG_WORK) config.numberOfRounds else 0,
            sparringRounds = if (config.workoutType == WorkoutType.SPARRING) config.numberOfRounds else 0,
            techniqueRounds = 0,
            padRounds = if (config.workoutType == WorkoutType.PAD_WORK) config.numberOfRounds else 0,
            shadowBoxingRounds = if (config.workoutType == WorkoutType.SHADOW_BOXING) config.numberOfRounds else 0,
            workoutType = config.workoutType
        )
    }
}
