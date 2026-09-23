package com.example.thecorner.ui.home

import com.example.thecorner.model.Workout
import com.example.thecorner.model.ProfileDefaults

data class HomeUiState(

    val userName: String = ProfileDefaults.DEFAULT_NAME,

    val workoutsThisWeek: Int = 0,
    val averageDuration: Int = 0,
    val averageCalories: Int = 0,
    val averageBagRounds: Int = 0,
    val lastWorkout: Workout? = null,
    val trainedDays: Set<Int> = emptySet()


)
