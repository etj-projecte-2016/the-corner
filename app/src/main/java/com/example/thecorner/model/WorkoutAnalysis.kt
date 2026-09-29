package com.example.thecorner.model

data class WorkoutAnalysis(
    val headline: String,
    val summary: String,
    val positives: List<String>,
    val improvements: List<String>,
    val nextSessionFocus: String,
)
