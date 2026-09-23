package com.example.thecorner

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.training.TrainingViewModel
import com.example.thecorner.ui.training.WorkoutConfigContract
import com.example.thecorner.ui.training.edit.EditWorkoutViewModel
import org.junit.Assert.*
import org.junit.Test

class WorkoutConfigTest {
    @Test fun configurationRoundTripAndInvalidType() {
        WorkoutType.entries.forEach { type ->
            val config = WorkoutConfig(120, 30, 4, type)
            assertEquals(config, WorkoutConfigContract.fromBundle(WorkoutConfigContract.toBundle(config)))
        }
        assertNull(WorkoutConfigContract.fromBundle(Bundle()))
        assertNull(WorkoutConfigContract.fromBundle(Bundle().apply {
            putString(WorkoutConfigContract.TYPE, "technique")
        }))
    }

    @Test fun editorDraftAndSavedTrainingConfigRestoreIndependently() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val trainingState = SavedStateHandle()
            val training = TrainingViewModel(trainingState)
            val original = WorkoutConfig(120, 30, 4, WorkoutType.PAD_WORK)
            training.setConfig(original)
            val editorState = SavedStateHandle()
            val editor = EditWorkoutViewModel(editorState)
            editor.initialize(WorkoutConfigContract.toBundle(original))
            editor.selectType(WorkoutType.SHADOW_BOXING)
            editor.increaseRounds()
            assertEquals(original, training.config.value) // Unsaved draft is isolated.
            val restoredEditor = EditWorkoutViewModel(SavedStateHandle(
                mapOf("config" to editorState.get<Bundle>("config"))
            ))
            restoredEditor.initialize(WorkoutConfigContract.toBundle(original))
            assertEquals(WorkoutType.SHADOW_BOXING, restoredEditor.workoutConfig.value?.workoutType)
            assertEquals(5, restoredEditor.workoutConfig.value?.numberOfRounds)
            training.setConfig(restoredEditor.workoutConfig.value!!)
            val restoredTraining = TrainingViewModel(SavedStateHandle(
                mapOf("config" to trainingState.get<Bundle>("config"))
            ))
            assertEquals(restoredEditor.workoutConfig.value, restoredTraining.config.value)
        }
    }
}
