package com.example.thecorner.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE workouts ADD COLUMN padRounds INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE workouts ADD COLUMN shadowBoxingRounds INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE workouts ADD COLUMN workoutType TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE workouts ADD COLUMN bodyWeightKgAtSession REAL")
        connection.execSQL("ALTER TABLE workouts ADD COLUMN activeDurationSeconds INTEGER")
        connection.execSQL("ALTER TABLE workouts ADD COLUMN restDurationSeconds INTEGER")
    }
}
