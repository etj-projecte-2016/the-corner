package com.example.thecorner.model

/** Null means insufficient/invalid historical data, not zero energy expenditure. */
fun Workout.estimatedCalories(): Double? {
    val type = workoutType ?: return null
    val weight = bodyWeightKgAtSession ?: WorkoutEnergyDefaults.DEFAULT_WEIGHT_KG
    if (!weight.isFinite() || weight <= 0 || totalRounds <= 0 || duration < 0) return null

    val active: Int
    val rest: Int
    if (activeDurationSeconds != null && restDurationSeconds != null) {
        active = activeDurationSeconds
        rest = restDurationSeconds
    } else if (activeDurationSeconds == null && restDurationSeconds == null && totalRounds == 1) {
        // Training never has a final rest: the single round accounts for the whole duration.
        active = duration
        rest = 0
    } else {
        // Total duration alone cannot tell us how much was work vs rest across multiple rounds.
        return null
    }
    if (active < 0 || rest < 0 || active.toLong() + rest != duration.toLong()) return null
    return CaloriesCalculator.calculate(type, weight, active, rest)
}
