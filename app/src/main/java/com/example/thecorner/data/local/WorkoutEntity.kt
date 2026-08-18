package com.example.thecorner.data.local

import androidx.room3.Entity
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
    val techniqueRounds: Int

)
