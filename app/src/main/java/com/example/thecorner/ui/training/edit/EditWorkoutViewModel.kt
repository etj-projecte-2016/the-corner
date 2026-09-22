package com.example.thecorner.ui.training.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.thecorner.model.WorkoutConfig

class EditWorkoutViewModel : ViewModel() {

    private val _workoutConfig = MutableLiveData(
        WorkoutConfig()
    )

    val workoutConfig: LiveData<WorkoutConfig> = _workoutConfig


    // ============================================================
    // ROUND DURATION
    // ============================================================

    fun increaseRoundDuration() {

        val currentConfig = _workoutConfig.value ?: return

        if (currentConfig.roundDurationSeconds < MAX_ROUND_DURATION) {

            _workoutConfig.value = currentConfig.copy(
                roundDurationSeconds =
                    currentConfig.roundDurationSeconds + TIME_STEP
            )
        }
    }

    fun decreaseRoundDuration() {

        val currentConfig = _workoutConfig.value ?: return

        if (currentConfig.roundDurationSeconds > MIN_ROUND_DURATION) {

            _workoutConfig.value = currentConfig.copy(
                roundDurationSeconds =
                    currentConfig.roundDurationSeconds - TIME_STEP
            )
        }
    }


    // ============================================================
    // REST DURATION
    // ============================================================

    fun increaseRestDuration() {

        val currentConfig = _workoutConfig.value ?: return

        if (currentConfig.restDurationSeconds < MAX_REST_DURATION) {

            _workoutConfig.value = currentConfig.copy(
                restDurationSeconds =
                    currentConfig.restDurationSeconds + TIME_STEP
            )
        }
    }

    fun decreaseRestDuration() {

        val currentConfig = _workoutConfig.value ?: return

        if (currentConfig.restDurationSeconds > MIN_REST_DURATION) {

            _workoutConfig.value = currentConfig.copy(
                restDurationSeconds =
                    currentConfig.restDurationSeconds - TIME_STEP
            )
        }
    }


    // ============================================================
    // ROUNDS
    // ============================================================

    fun increaseRounds() {

        val currentConfig = _workoutConfig.value ?: return

        if (currentConfig.numberOfRounds < MAX_ROUNDS) {

            _workoutConfig.value = currentConfig.copy(
                numberOfRounds =
                    currentConfig.numberOfRounds + 1
            )
        }
    }

    fun decreaseRounds() {

        val currentConfig = _workoutConfig.value ?: return

        if (currentConfig.numberOfRounds > MIN_ROUNDS) {

            _workoutConfig.value = currentConfig.copy(
                numberOfRounds =
                    currentConfig.numberOfRounds - 1
            )
        }
    }


    companion object {

        private const val TIME_STEP = 30

        private const val MIN_ROUND_DURATION = 30
        private const val MAX_ROUND_DURATION = 600

        private const val MIN_REST_DURATION = 0
        private const val MAX_REST_DURATION = 300

        private const val MIN_ROUNDS = 1
        private const val MAX_ROUNDS = 30
    }
}