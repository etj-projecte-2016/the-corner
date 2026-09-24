package com.example.thecorner.ui.ai

data class AIConfig(
    val modelName: String = "gemini-3.8-flash",
    /** Request timeout in milliseconds. */
    val timeout: Long = 30_000L,
    val maxRetries: Int = 2,
    /** Initial retry delay in milliseconds. */
    val initialBackoff: Long = 500L,
)
