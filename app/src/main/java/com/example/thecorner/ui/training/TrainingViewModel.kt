package com.example.thecorner.ui.training

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.thecorner.model.WorkoutConfig

class TrainingViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _config = MutableLiveData(
        WorkoutConfigContract.fromBundle(savedState.get<Bundle>("config")) ?: WorkoutConfig()
    )
    val config: LiveData<WorkoutConfig> = _config

    fun setConfig(config: WorkoutConfig) {
        savedState["config"] = WorkoutConfigContract.toBundle(config)
        _config.value = config
    }
}
