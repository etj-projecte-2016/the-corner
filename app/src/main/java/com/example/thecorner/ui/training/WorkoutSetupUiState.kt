package com.example.thecorner.ui.training

data class WorkoutSetupUiState(
    val roundDuration: Int = 180,
    val restDuration: Int = 60,
    val rounds: Int = 10
)