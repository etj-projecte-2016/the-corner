package com.example.thecorner.ui.training

import android.os.Bundle
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType

object WorkoutConfigContract {
    const val RESULT = "workoutConfigResult"
    const val ROUND_DURATION = "roundDurationSeconds"
    const val REST_DURATION = "restDurationSeconds"
    const val ROUNDS = "numberOfRounds"
    const val TYPE = "workoutType"

    fun toBundle(config: WorkoutConfig) = Bundle().apply {
        putInt(ROUND_DURATION, config.roundDurationSeconds)
        putInt(REST_DURATION, config.restDurationSeconds)
        putInt(ROUNDS, config.numberOfRounds)
        putString(TYPE, config.workoutType.storageId)
    }

    fun fromBundle(bundle: Bundle?): WorkoutConfig? {
        bundle ?: return null
        val type = WorkoutType.fromStorageId(bundle.getString(TYPE)) ?: return null
        val round = bundle.getInt(ROUND_DURATION, 180)
        val rest = bundle.getInt(REST_DURATION, 60)
        val rounds = bundle.getInt(ROUNDS, 10)
        if (round !in 30..600 || rest !in 0..300 || rounds !in 1..30) return null
        return WorkoutConfig(round, rest, rounds, type)
    }
}
