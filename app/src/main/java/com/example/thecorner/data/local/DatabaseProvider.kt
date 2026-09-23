package com.example.thecorner.data.local

import android.content.Context
import androidx.room3.Room


object DatabaseProvider {

    @Volatile
    private var INSTANCE: WorkoutDatabase? = null

    fun getDatabase(context: Context): WorkoutDatabase {
        return INSTANCE ?: synchronized(this) {

            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                WorkoutDatabase::class.java,
                "the_corner_database"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also {
                INSTANCE = it
            }
        }
    }
}
