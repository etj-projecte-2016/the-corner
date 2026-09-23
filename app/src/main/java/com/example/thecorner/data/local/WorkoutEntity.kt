package com.example.thecorner.data.local

import androidx.room3.Entity
import androidx.room3.ColumnInfo
import androidx.room3.PrimaryKey


@Entity(tableName = "workouts")
class WorkoutEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val duration: Int,
    val calories: Int,
    val totalRounds: Int,
    val bagRounds: Int,
    val sparringRounds: Int,
    val techniqueRounds: Int,
    @ColumnInfo(defaultValue = "0") val padRounds: Int = 0,
    @ColumnInfo(defaultValue = "0") val shadowBoxingRounds: Int = 0,
    val workoutType: String? = null,
    val bodyWeightKgAtSession: Double? = null,
    val activeDurationSeconds: Int? = null,
    val restDurationSeconds: Int? = null

)
