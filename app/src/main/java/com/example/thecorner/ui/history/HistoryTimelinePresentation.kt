package com.example.thecorner.ui.history

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.thecorner.R
import com.example.thecorner.model.WorkoutType
import java.util.Locale
import kotlin.math.roundToLong

// Artwork shared by History, Home and Session Details; replace images here for all three screens.
@DrawableRes
internal fun WorkoutType?.historyCardImage(): Int = when (this) {
    WorkoutType.BAG_WORK -> R.drawable.bag // history_bag.webp
    WorkoutType.PAD_WORK -> R.drawable.pad_work // history_pads.webp
    WorkoutType.SPARRING -> R.drawable.countdown_boxer // history_sparring.webp
    WorkoutType.SHADOW_BOXING -> R.drawable.shadow_boxing // history_technique.webp
    null -> R.drawable.ic_boxing_glove
}

internal fun historyCompactDuration(context: Context, seconds: Long): String = when {
    seconds >= 3600 -> context.getString(R.string.history_time_hours, seconds / 3600, seconds % 3600 / 60)
    seconds % 60 == 0L -> context.getString(R.string.history_minutes, seconds / 60)
    seconds < 60 -> context.getString(R.string.history_time_seconds, seconds)
    else -> context.getString(R.string.history_time_minutes_seconds, seconds / 60, seconds % 60)
}

internal fun HistorySummary.dayCaption(context: Context): String {
    val count = context.resources.getQuantityString(R.plurals.history_session_count, sessions, sessions)
    val time = historyCompactDuration(context, durationSeconds)
    val base = context.getString(R.string.history_session_summary, count, time)
    return (estimatedCalories?.let {
        context.getString(R.string.history_session_summary, base,
            context.getString(R.string.history_kcal_compact, it.roundToLong()))
    } ?: base).uppercase(Locale.getDefault())
}
