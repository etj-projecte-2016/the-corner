package com.example.thecorner.debug

import android.content.Context
import android.widget.Toast
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Development-only data actions. This source file is not compiled into release variants. */
object DebugWorkoutSeeder {
    private const val PREFS = "debug_workout_seeder"
    private const val SEEDED_IDS = "seeded_workout_ids"
    private const val WEIGHT_KG = 75.0
    private val fixedSessionTime = LocalTime.of(10, 30)

    private data class SessionSpec(
        val daysAgo: Long,
        val rounds: Int,
        val roundSeconds: Int,
        val restSeconds: Int,
        val type: WorkoutType
    )

    private val sessions = listOf(
        SessionSpec(20, 5, 120, 45, WorkoutType.SHADOW_BOXING),
        SessionSpec(18, 4, 180, 60, WorkoutType.BAG_WORK),
        SessionSpec(16, 6, 150, 45, WorkoutType.PAD_WORK),
        SessionSpec(14, 5, 180, 60, WorkoutType.SPARRING),
        SessionSpec(12, 8, 120, 30, WorkoutType.SHADOW_BOXING),
        SessionSpec(10, 6, 150, 60, WorkoutType.BAG_WORK),
        SessionSpec(8, 4, 180, 45, WorkoutType.PAD_WORK),
        SessionSpec(6, 7, 120, 60, WorkoutType.SPARRING),
        SessionSpec(4, 5, 180, 30, WorkoutType.SHADOW_BOXING),
        SessionSpec(3, 8, 180, 60, WorkoutType.BAG_WORK),
        SessionSpec(1, 6, 150, 45, WorkoutType.PAD_WORK),
        // Deliberately last: the best Analyze My Last Session fixture.
        SessionSpec(0, 6, 180, 60, WorkoutType.BAG_WORK)
    )

    @JvmStatic
    fun seed(context: Context) {
        runInBackground(context) { repository, workouts, prefs ->
            val expected = buildWorkouts()
            val knownIds = prefs.getStringSet(SEEDED_IDS, emptySet()).orEmpty()
                .mapNotNull { it.toLongOrNull() }.toMutableSet()
            val existing = workouts.toMutableList()

            expected.forEach { wanted ->
                val match = existing.firstOrNull { it.id in knownIds && it.matches(wanted) }
                if (match != null) {
                    knownIds += match.id
                } else {
                    repository.insertWorkout(wanted)
                    val inserted = repository.getAllWorkouts().first()
                        .firstOrNull { it.matches(wanted) }
                    if (inserted != null) {
                        knownIds += inserted.id
                        existing += inserted
                        prefs.edit().putStringSet(SEEDED_IDS, knownIds.map(Long::toString).toSet()).apply()
                    }
                }
            }
            prefs.edit().putStringSet(SEEDED_IDS, knownIds.map(Long::toString).toSet()).apply()
            "Debug workout data ready (${expected.size} sessions)"
        }
    }

    @JvmStatic
    fun clear(context: Context) {
        runInBackground(context) { repository, _, prefs ->
            val ids = prefs.getStringSet(SEEDED_IDS, emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }
            repository.deleteWorkoutsById(ids)
            prefs.edit().remove(SEEDED_IDS).apply()
            "Cleared ${ids.size} seeded workouts"
        }
    }

    private fun runInBackground(context: Context, action: suspend (com.example.thecorner.data.repository.WorkoutRepository, List<Workout>, android.content.SharedPreferences) -> String) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val app = appContext as TheCornerApplication
            val repository = app.appContainer.workoutRepository
            val message = action(repository, repository.getAllWorkouts().first(), appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE))
            withContext(Dispatchers.Main) {
                Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buildWorkouts(): List<Workout> {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        return sessions.map { spec ->
            val date = today.minusDays(spec.daysAgo).atTime(fixedSessionTime).atZone(zone).toInstant().toEpochMilli()
            Workout.completed(
                WorkoutConfig(spec.roundSeconds, spec.restSeconds, spec.rounds, spec.type),
                date,
                WEIGHT_KG
            )
        }
    }

    private fun Workout.matches(other: Workout): Boolean =
        date == other.date && duration == other.duration && calories == other.calories &&
            totalRounds == other.totalRounds && bagRounds == other.bagRounds &&
            sparringRounds == other.sparringRounds && techniqueRounds == other.techniqueRounds &&
            padRounds == other.padRounds && shadowBoxingRounds == other.shadowBoxingRounds &&
            workoutType == other.workoutType && bodyWeightKgAtSession == other.bodyWeightKgAtSession &&
            activeDurationSeconds == other.activeDurationSeconds && restDurationSeconds == other.restDurationSeconds
}
