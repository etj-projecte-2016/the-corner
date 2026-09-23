package com.example.thecorner

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room3.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.thecorner.data.local.MIGRATION_1_2
import com.example.thecorner.data.local.MIGRATION_2_3
import com.example.thecorner.data.local.WorkoutDatabase
import com.example.thecorner.data.local.toEntity
import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class WorkoutMigrationTest {
    @Test fun migrationPreservesHistoryAndAcceptsAllFourTypes() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "workout-migration-test.db"
        context.deleteDatabase(name)
        try {
            context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
                old.execSQL("CREATE TABLE workouts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date INTEGER NOT NULL, duration INTEGER NOT NULL, calories INTEGER NOT NULL, totalRounds INTEGER NOT NULL, bagRounds INTEGER NOT NULL, sparringRounds INTEGER NOT NULL, techniqueRounds INTEGER NOT NULL)")
                old.execSQL("INSERT INTO workouts VALUES (1, 100, 900, 0, 5, 1, 1, 3)")
                old.execSQL("INSERT INTO workouts VALUES (2, 200, 900, 0, 5, 0, 0, 0)")
                old.version = 1
            }
            val database = Room.databaseBuilder(context, WorkoutDatabase::class.java, name)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
            try {
                val history = database.workoutDao().getAllWorkouts().first()
                assertEquals(2, history.size)
                val classified = history.first { it.id == 1L }
                assertEquals(1, classified.bagRounds)
                assertEquals(1, classified.sparringRounds)
                assertEquals(3, classified.techniqueRounds)
                history.forEach {
                    assertNull(it.workoutType)
                    assertEquals(0, it.padRounds)
                    assertEquals(0, it.shadowBoxingRounds)
                    assertEquals(5, it.totalRounds)
                    assertNull(it.bodyWeightKgAtSession)
                    assertNull(it.activeDurationSeconds)
                    assertNull(it.restDurationSeconds)
                }
                WorkoutType.entries.forEach { type ->
                    database.workoutDao().insertWorkout(
                        Workout.completed(WorkoutConfig(workoutType = type), 300L).toEntity()
                    )
                }
                assertEquals(6, database.workoutDao().getAllWorkouts().first().size)
            } finally {
                database.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
