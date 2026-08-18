package com.example.thecorner.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts ORDER BY date DESC LIMIT 1")
    fun getLastWorkout(): Flow<WorkoutEntity?>

}