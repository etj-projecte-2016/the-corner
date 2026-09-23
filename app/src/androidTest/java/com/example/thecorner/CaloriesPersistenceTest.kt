package com.example.thecorner

import android.content.Context
import androidx.room3.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.thecorner.data.local.MIGRATION_2_3
import com.example.thecorner.data.local.WorkoutDatabase
import com.example.thecorner.data.repository.WorkoutRepository
import com.example.thecorner.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CaloriesPersistenceTest {
    @Test fun migrationPreservesVersionTwoRecordsAndLeavesSnapshotsUnknown() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "calories-migration-test.db"
        context.deleteDatabase(name)
        try {
            context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
                old.execSQL("CREATE TABLE workouts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date INTEGER NOT NULL, duration INTEGER NOT NULL, calories INTEGER NOT NULL, totalRounds INTEGER NOT NULL, bagRounds INTEGER NOT NULL, sparringRounds INTEGER NOT NULL, techniqueRounds INTEGER NOT NULL, padRounds INTEGER NOT NULL DEFAULT 0, shadowBoxingRounds INTEGER NOT NULL DEFAULT 0, workoutType TEXT)")
                WorkoutType.entries.forEachIndexed { index, type ->
                    old.execSQL("INSERT INTO workouts VALUES (?, ?, 2340, 123, 10, 1, 2, 3, 1, 3, ?)",
                        arrayOf<Any>(index + 1, 100L + index, type.storageId))
                }
                old.execSQL("INSERT INTO workouts VALUES (5, 200, 180, 0, 1, 1, 0, 0, 0, 0, 'bag_work')")
                old.version = 2
            }
            val database = Room.databaseBuilder(context, WorkoutDatabase::class.java, name)
                .addMigrations(MIGRATION_2_3).build()
            try {
                val repository = WorkoutRepository(database.workoutDao())
                val records = repository.getAllWorkouts().first()
                assertEquals(5, records.size)
                WorkoutType.entries.forEachIndexed { index, type ->
                    val session = records.single { it.id == index + 1L }
                    assertEquals(type, session.workoutType)
                    assertEquals(100L + index, session.date)
                    assertEquals(2340, session.duration)
                    assertEquals(123, session.calories)
                    assertEquals(10, session.totalRounds)
                    assertEquals(listOf(1, 2, 3, 1, 3), listOf(session.bagRounds, session.sparringRounds,
                        session.techniqueRounds, session.padRounds, session.shadowBoxingRounds))
                    assertNull(session.estimatedCalories()) // Cannot infer work/rest from a total.
                }
                records.forEach {
                    assertNull(it.bodyWeightKgAtSession)
                    assertNull(it.activeDurationSeconds)
                    assertNull(it.restDurationSeconds)
                }
                // One legacy round has no final rest, so a fallback-weight estimate is possible.
                val single = repository.getWorkoutById(5).first()!!
                assertEquals(5.8 * 3.5 * WorkoutEnergyDefaults.DEFAULT_WEIGHT_KG / 200 * 3,
                    single.estimatedCalories()!!, 0.000001)
                assertNull(repository.getWorkoutById(5).first()!!.bodyWeightKgAtSession)
            } finally { database.close() }
        } finally { context.deleteDatabase(name) }
    }

    @Test fun allTypesRetainTheirWeightAndDurationsAfterDatabaseReopens() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "calories-reopen-test.db"
        context.deleteDatabase(name)
        val sessions = WorkoutType.entries.mapIndexed { index, type ->
            Workout.completed(WorkoutConfig(180, 60, 10, type), index.toLong()).copy(id = index + 1L)
        } + Workout.completed(WorkoutConfig(), 10, weightKg = 82.0).copy(id = 5)
        try {
            val initial = Room.databaseBuilder(context, WorkoutDatabase::class.java, name).build()
            try {
                val repository = WorkoutRepository(initial.workoutDao())
                sessions.forEach { repository.insertWorkout(it) }
            } finally { initial.close() }
            repeat(2) {
                val reopened = Room.databaseBuilder(context, WorkoutDatabase::class.java, name).build()
                try {
                    val repository = WorkoutRepository(reopened.workoutDao())
                    assertEquals(5, repository.getAllWorkouts().first().size)
                    sessions.forEach { expected ->
                        val actual = repository.getWorkoutById(expected.id).first()!!
                        assertEquals(expected, actual)
                        assertEquals(expected.estimatedCalories()!!, actual.estimatedCalories()!!, 0.000001)
                    }
                    assertEquals(WorkoutEnergyDefaults.DEFAULT_WEIGHT_KG,
                        repository.getWorkoutById(1).first()!!.bodyWeightKgAtSession!!, 0.0)
                } finally { reopened.close() }
            }
        } finally { context.deleteDatabase(name) }
    }
}
