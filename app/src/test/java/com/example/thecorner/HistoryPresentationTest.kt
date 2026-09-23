package com.example.thecorner

import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.history.historyMonths
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class HistoryPresentationTest {
    private fun workout(id: Long, date: String, type: WorkoutType = WorkoutType.BAG_WORK) =
        Workout.completed(WorkoutConfig(workoutType = type), Instant.parse(date).toEpochMilli()).copy(id = id)

    @Test fun allTypesFilterWithoutReclassifyingHistoricalRecords() {
        val sessions = WorkoutType.entries.mapIndexed { i, type ->
            workout(i.toLong(), "2026-09-22T19:42:00Z", type)
        } + workout(99, "2026-09-21T19:42:00Z").copy(workoutType = null, techniqueRounds = 5)
        WorkoutType.entries.forEach { type ->
            val filtered = historyMonths(sessions, type).flatMap { it.sessions }
            assertEquals(1, filtered.size)
            assertEquals(type, filtered.single().workoutType)
        }
        assertEquals(5, historyMonths(sessions, null).flatMap { it.sessions }.size)
    }

    @Test fun newestFirstWithDeterministicTiesAndSeparateYears() {
        val sessions = listOf(
            workout(1, "2025-09-22T10:00:00Z"), workout(2, "2026-08-31T10:00:00Z"),
            workout(3, "2026-09-22T10:00:00Z"), workout(4, "2026-09-22T19:00:00Z"),
            workout(5, "2026-09-22T19:00:00Z")
        )
        val months = historyMonths(sessions, null, ZoneId.of("UTC"))
        assertEquals(listOf(YearMonth.of(2026, 9), YearMonth.of(2026, 8), YearMonth.of(2025, 9)), months.map { it.month })
        assertEquals(listOf(5L, 4L, 3L, 2L, 1L), months.flatMap { it.sessions }.map { it.id })
    }

    @Test fun monthBoundaryUsesDisplayTimeZoneAndEmptyFiltersStayEmpty() {
        val session = workout(1, "2026-08-31T23:30:00Z")
        assertEquals(YearMonth.of(2026, 9), historyMonths(listOf(session), null, ZoneId.of("Europe/Madrid")).single().month)
        assertEquals(YearMonth.of(2026, 8), historyMonths(listOf(session), null, ZoneId.of("UTC")).single().month)
        assertTrue(historyMonths(emptyList(), null).isEmpty())
        assertTrue(historyMonths(listOf(session), WorkoutType.PAD_WORK).isEmpty())
    }
}
