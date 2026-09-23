package com.example.thecorner.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [WorkoutEntity::class],
    version = 3,
    exportSchema = true
)

abstract class WorkoutDatabase: RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
}
