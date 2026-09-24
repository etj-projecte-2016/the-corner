package com.example.thecorner.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AiViewModelFactory(
    private val service: AIService,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiViewModel::class.java)) {
            return AiViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
