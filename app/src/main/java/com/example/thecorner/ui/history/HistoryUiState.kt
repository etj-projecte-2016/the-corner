package com.example.thecorner.ui.history

import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.model.estimatedCalories
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class HistoryMonth(
    val month: YearMonth,
    val sessions: List<Workout>,
    val days: List<HistoryDay> = historyDays(sessions)
)

data class HistoryDay(val date: LocalDate, val sessions: List<Workout>, val summary: HistorySummary)

data class HistorySummary(
    val sessions: Int = 0,
    val durationSeconds: Long = 0,
    val estimatedCalories: Double? = null,
    val sessionsThisMonth: Int = 0
)

data class HistoryUiState(
    val months: List<HistoryMonth> = emptyList(),
    val selectedFilter: WorkoutType? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val summary: HistorySummary = HistorySummary()
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
    .map { (month, workouts) -> HistoryMonth(month, workouts, historyDays(workouts, zone)) }

internal fun historyDays(sessions: List<Workout>, zone: ZoneId = ZoneId.systemDefault()): List<HistoryDay> =
    sessions.groupBy { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }
        .map { (date, workouts) -> HistoryDay(date, workouts, historySummary(workouts, zone)) }

internal fun historySummary(sessions: List<Workout>, zone: ZoneId = ZoneId.systemDefault()): HistorySummary {
    val estimates = sessions.map { it.estimatedCalories() }
    val currentMonth = YearMonth.now(zone)
    return HistorySummary(
        sessions = sessions.size,
        durationSeconds = sessions.sumOf { it.duration.toLong() },
        // A total is only meaningful if every session in the group has an estimate.
        estimatedCalories = if (estimates.isNotEmpty() && estimates.all { it != null }) estimates.filterNotNull().sum() else null,
        sessionsThisMonth = sessions.count { YearMonth.from(Instant.ofEpochMilli(it.date).atZone(zone)) == currentMonth }
    )
}
