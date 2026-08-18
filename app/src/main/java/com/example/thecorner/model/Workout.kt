package com.example.thecorner.model

data class Workout (

    val id: Long,
    val date: Long,
    val duration: Int,
    val calories: Int,
    val totalRounds: Int,
    val bagRounds: Int,
    val sparringRounds: Int,
    val techniqueRounds: Int

)