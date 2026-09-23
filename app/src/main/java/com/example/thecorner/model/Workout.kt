package com.example.thecorner.model

import kotlin.math.roundToInt

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
    val workoutType: WorkoutType? = null,
    val bodyWeightKgAtSession: Double? = null,
    val activeDurationSeconds: Int? = null,
    val restDurationSeconds: Int? = null

) {
    companion object {
        fun completed(
            config: WorkoutConfig,
            date: Long,
            weightKg: Double = WorkoutEnergyDefaults.DEFAULT_WEIGHT_KG
        ): Workout = Workout(
            id = 0,
            date = date,
            duration = config.activeDurationSeconds + config.totalRestDurationSeconds,
            calories = CaloriesCalculator.calculate(config.workoutType, weightKg,
                config.activeDurationSeconds, config.totalRestDurationSeconds).roundToInt(),
            totalRounds = config.numberOfRounds,
            bagRounds = if (config.workoutType == WorkoutType.BAG_WORK) config.numberOfRounds else 0,
            sparringRounds = if (config.workoutType == WorkoutType.SPARRING) config.numberOfRounds else 0,
            techniqueRounds = 0,
            padRounds = if (config.workoutType == WorkoutType.PAD_WORK) config.numberOfRounds else 0,
            shadowBoxingRounds = if (config.workoutType == WorkoutType.SHADOW_BOXING) config.numberOfRounds else 0,
            workoutType = config.workoutType,
            bodyWeightKgAtSession = weightKg,
            activeDurationSeconds = config.activeDurationSeconds,
            restDurationSeconds = config.totalRestDurationSeconds
        )
    }
}
