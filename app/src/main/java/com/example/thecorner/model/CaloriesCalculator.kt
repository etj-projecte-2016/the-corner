package com.example.thecorner.model

// Initial estimation coefficients, not measurements of individual energy expenditure.
val WorkoutType.met: Double
    get() = when (this) {
        WorkoutType.BAG_WORK -> 5.8
        WorkoutType.PAD_WORK -> 9.3
        WorkoutType.SHADOW_BOXING -> 5.3
        WorkoutType.SPARRING -> 7.8
    }

object CaloriesCalculator {
    const val REST_MET = 1.5

    /** Durations are seconds of work/rest; exclude preparation, pauses and any final rest. */
    fun calculate(
        workoutType: WorkoutType,
        weightKg: Double,
        activeDurationSeconds: Int,
        restDurationSeconds: Int
    ): Double {
        require(weightKg.isFinite() && weightKg > 0) { "Weight must be finite and positive" }
        require(activeDurationSeconds >= 0 && restDurationSeconds >= 0) { "Durations cannot be negative" }
        // kcal/min = MET * 3.5 * weightKg / 200; round only when displaying/storing legacy kcal.
        val kcalPerMetMinute = 3.5 * weightKg / 200.0
        val activeMinutes = activeDurationSeconds / 60.0
        val restMinutes = restDurationSeconds / 60.0
        return kcalPerMetMinute * (workoutType.met * activeMinutes + REST_MET * restMinutes)
    }
}
