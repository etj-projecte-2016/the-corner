package com.example.thecorner.ui.ai

interface AIService {
    suspend fun runConnectivityTest(): AIResult<String>
}
