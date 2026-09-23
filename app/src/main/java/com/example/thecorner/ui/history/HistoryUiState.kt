package com.example.thecorner.ui.history

import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutType
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

data class HistoryMonth(val month: YearMonth, val sessions: List<Workout>)

data class HistoryUiState(
    val months: List<HistoryMonth> = emptyList(),
    val selectedFilter: WorkoutType? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false
) {
    val isEmpty: Boolean get() = !isLoading && !hasError && months.isEmpty()
}

// Month boundaries use the device time zone, just like the displayed dates.
internal fun historyMonths(
    sessions: List<Workout>,
    filter: WorkoutType?,
    zone: ZoneId = ZoneId.systemDefault()
): List<HistoryMonth> = sessions.asSequence()
    .filter { filter == null || it.workoutType == filter }
    .sortedWith(compareByDescending<Workout> { it.date }.thenByDescending { it.id })
    .groupBy { YearMonth.from(Instant.ofEpochMilli(it.date).atZone(zone)) }
    .map { (month, workouts) -> HistoryMonth(month, workouts) }
