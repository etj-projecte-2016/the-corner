package com.example.thecorner.di

import android.content.Context
import com.example.thecorner.data.local.DatabaseProvider
import com.example.thecorner.data.repository.WorkoutRepository

class AppContainer(context: Context) {

    private val database = DatabaseProvider.getDatabase(context)

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(database.workoutDao())
    }

}