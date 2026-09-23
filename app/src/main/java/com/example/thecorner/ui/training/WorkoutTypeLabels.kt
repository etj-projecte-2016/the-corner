package com.example.thecorner.ui.training

import androidx.annotation.StringRes
import com.example.thecorner.R
import com.example.thecorner.model.WorkoutType

@StringRes
fun WorkoutType.labelRes(): Int = when (this) {
    WorkoutType.BAG_WORK -> R.string.workout_type_bag
    WorkoutType.PAD_WORK -> R.string.workout_type_pad
    WorkoutType.SPARRING -> R.string.workout_type_sparring
    WorkoutType.SHADOW_BOXING -> R.string.workout_type_shadow
}
