package com.example.thecorner.ui.history

import android.content.Context
import com.example.thecorner.R
import com.example.thecorner.model.Workout
import com.example.thecorner.ui.training.labelRes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal fun Workout.displayTitle(context: Context): String =
    context.getString(workoutType?.labelRes() ?: R.string.home_boxing).uppercase(Locale.getDefault())

internal fun sessionDate(timestamp: Long): String = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    .format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))

internal fun sessionTime(timestamp: Long): String = DateTimeFormatter
    .ofPattern("HH:mm", Locale.getDefault())
    .format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))

internal fun sessionDuration(context: Context, seconds: Int): String =
    if (seconds % 60 == 0) context.getString(R.string.history_minutes, seconds / 60)
    else context.getString(R.string.history_minutes_seconds, seconds / 60, seconds % 60)
