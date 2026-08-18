package com.example.thecorner.data.repository

import com.example.thecorner.data.local.WorkoutDao
import com.example.thecorner.data.local.toDomain
import com.example.thecorner.data.local.toEntity
import com.example.thecorner.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepository(
    private val workoutDao: WorkoutDao
) {

    suspend fun insertWorkout(workout: Workout) {
        workoutDao.insertWorkout(workout.toEntity())
    }

    suspend fun getAllWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts()
            .map { workouts ->
                workouts.map { it.toDomain() }
            }
    }

    fun getLastWorkout(): Flow<Workout?> {
        return workoutDao.getLastWorkout()
            .map { it?.toDomain() }
    }
}