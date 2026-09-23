package com.example.thecorner.ui.history

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.thecorner.R
import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.training.labelRes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal fun Workout.displayTitle(context: Context): String =
    context.getString(workoutType?.labelRes() ?: R.string.home_boxing).uppercase(Locale.getDefault())

// Replace these mappings with history_bag/pads/shadow/sparring.webp when available.
// Existing photography is reused; unclassified history uses a discreet vector placeholder.
@DrawableRes
internal fun WorkoutType?.historyImage(): Int = when (this) {
    WorkoutType.BAG_WORK -> R.drawable.bag
    WorkoutType.PAD_WORK -> R.drawable.guantes
    WorkoutType.SPARRING, WorkoutType.SHADOW_BOXING -> R.drawable.boxer
    null -> R.drawable.ic_boxing_glove
}

internal fun sessionDate(timestamp: Long): String = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    .format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))

internal fun sessionTime(timestamp: Long): String = DateTimeFormatter
    .ofPattern("HH:mm", Locale.getDefault())
    .format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))

internal fun sessionDuration(context: Context, seconds: Int): String =
    if (seconds % 60 == 0) context.getString(R.string.history_minutes, seconds / 60)
    else context.getString(R.string.history_minutes_seconds, seconds / 60, seconds % 60)
