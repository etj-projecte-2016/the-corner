package com.example.thecorner.ui.ai

data class AIConfig(
    val modelName: String = "gemini-3.5-flash",
    val fallbackModelName: String = "gemini-3.5-flash",
    /** Request timeout in milliseconds. */
    val timeout: Long = 30_000L,
    val maxRetries: Int = 2,
)
