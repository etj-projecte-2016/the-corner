package com.example.thecorner.model

data class Workout (

    val id: Long,
    val date: Long,
    // Configured rounds plus rests between rounds, in seconds; excludes pauses and preparation.
    val duration: Int,
    val calories: Int,
    val totalRounds: Int,
    // Classified counts only. totalRounds minus these three counts is unclassified.
    val bagRounds: Int,
    val sparringRounds: Int,
    val techniqueRounds: Int

)
