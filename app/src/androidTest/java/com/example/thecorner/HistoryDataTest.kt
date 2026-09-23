package com.example.thecorner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.room3.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.thecorner.data.local.WorkoutDatabase
import com.example.thecorner.data.repository.WorkoutRepository
import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.history.HistoryViewModel
import com.example.thecorner.ui.history.SessionDetailsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class HistoryDataTest {
    @Test fun roomRepositoryObservesCompletionAndQueriesExactId() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java).build()
        try {
            val repository = WorkoutRepository(database.workoutDao())
            assertTrue(repository.getAllWorkouts().first().isEmpty())
            assertNull(repository.getWorkoutById(77).first())
            val observed = async(start = CoroutineStart.UNDISPATCHED) {
                withTimeout(5_000) { repository.getAllWorkouts().first { it.size == 4 } }
            }
            WorkoutType.entries.forEachIndexed { index, type ->
                repository.insertWorkout(Workout.completed(WorkoutConfig(120, 30, 4, type), 100L)
                    .copy(id = index + 1L))
            }
            assertEquals(listOf(4L, 3L, 2L, 1L), observed.await().map { it.id })
            val session = repository.getWorkoutById(2).first()!!
            assertEquals(WorkoutType.PAD_WORK, session.workoutType)
            assertEquals(570, session.duration)
            assertEquals(4, session.totalRounds)
            assertEquals(4L, repository.getLastWorkout().first()?.id)
        } finally { database.close() }
    }

    @Test fun viewModelsFilterRestoreObserveAndHandleMissingSession() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java).build()
        val store = ViewModelStore()
        try {
            val repository = WorkoutRepository(database.workoutDao())
            val saved = SavedStateHandle()
            val history = withContext(Dispatchers.Main) { HistoryViewModel(repository, saved).also { store.put("history", it) } }
            assertTrue(withTimeout(5_000) { history.uiState.first { !it.isLoading } }.isEmpty)
            repository.insertWorkout(Workout.completed(WorkoutConfig(workoutType = WorkoutType.BAG_WORK), 100).copy(id = 1))
            repository.insertWorkout(Workout.completed(WorkoutConfig(workoutType = WorkoutType.PAD_WORK), 200).copy(id = 2))
            assertEquals(listOf(2L, 1L), withTimeout(5_000) {
                history.uiState.first { it.months.sumOf { month -> month.sessions.size } == 2 }
            }.months.flatMap { it.sessions }.map { it.id })
            repeat(3) {
                for (type in WorkoutType.entries) {
                    withContext(Dispatchers.Main) { history.selectFilter(type) }
                    val state = withTimeout(5_000) { history.uiState.first { !it.isLoading && it.selectedFilter == type } }
                    assertTrue(state.months.flatMap { it.sessions }.all { it.workoutType == type })
                    if (type == WorkoutType.SHADOW_BOXING || type == WorkoutType.SPARRING) assertTrue(state.isEmpty)
                }
            }
            withContext(Dispatchers.Main) { history.selectFilter(WorkoutType.PAD_WORK) }
            val restored = withContext(Dispatchers.Main) {
                HistoryViewModel(repository, SavedStateHandle(mapOf("history_filter" to saved.get<String>("history_filter"))))
                    .also { store.put("restored", it) }
            }
            assertEquals(2L, withTimeout(5_000) { restored.uiState.first { !it.isLoading } }.months.single().sessions.single().id)
            val details = withContext(Dispatchers.Main) { SessionDetailsViewModel(repository, 2).also { store.put("details", it) } }
            assertEquals(WorkoutType.PAD_WORK, withTimeout(5_000) { details.uiState.first { !it.isLoading } }.session?.workoutType)
            val missing = withContext(Dispatchers.Main) { SessionDetailsViewModel(repository, 99).also { store.put("missing", it) } }
            val state = withTimeout(5_000) { missing.uiState.first { !it.isLoading } }
            assertNull(state.session)
            assertFalse(state.hasError)
        } finally {
            withContext(Dispatchers.Main) { store.clear() }
            database.close()
        }
    }
}
