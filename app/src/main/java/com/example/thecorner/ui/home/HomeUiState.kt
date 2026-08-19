package com.example.thecorner.ui.home

import com.example.thecorner.model.Workout

data class HomeUiState(

    val workoutsThisWeek: Int = 0,
    val averageDuration: Int = 0,
    val averageCalories: Int = 0,
    val averageBagRounds: Int = 0,
    val lastWorkout: Workout? = null,
    val trainedDays: Set<Int> = emptySet()


)