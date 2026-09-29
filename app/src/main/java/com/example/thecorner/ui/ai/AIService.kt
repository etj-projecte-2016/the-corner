package com.example.thecorner.ui.ai

import com.example.thecorner.model.WorkoutAnalysis

interface AIService {
    val isConfigured: Boolean
        get() = true

    suspend fun analyzeLastSession(prompt: String): AIResult<WorkoutAnalysis>
}
