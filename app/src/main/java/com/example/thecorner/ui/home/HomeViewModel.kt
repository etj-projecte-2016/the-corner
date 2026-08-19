package com.example.thecorner.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.model.Workout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        (application as TheCornerApplication)
            .appContainer
            .workoutRepository

    val uiState: StateFlow<HomeUiState> =
        repository.getAllWorkouts()
            .map { workouts ->

                val weeklyWorkouts = getWorkoutsThisWeek(workouts)
                val trainedDays = getTrainedDays(weeklyWorkouts)

                HomeUiState(
                    workoutsThisWeek = weeklyWorkouts.size,
                    averageDuration = calculateAverageDuration(weeklyWorkouts),
                    averageCalories = calculateAverageCalories(weeklyWorkouts),
                    averageBagRounds = calculateAverageBagRounds(weeklyWorkouts),
                    lastWorkout = workouts.firstOrNull(),
                    trainedDays = trainedDays
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState()
            )

    private fun calculateAverageDuration(
        workouts: List<Workout>
    ): Int {
        if (workouts.isEmpty()) return 0

        return workouts
            .map { it.duration }
            .average()
            .toInt()
    }

    private fun calculateAverageCalories(
        workouts: List<Workout>
    ): Int {
        if (workouts.isEmpty()) return 0

        return workouts
            .map { it.calories }
            .average()
            .toInt()
    }

    private fun calculateAverageBagRounds(
        workouts: List<Workout>
    ): Int {
        if (workouts.isEmpty()) return 0

        return workouts
            .map { it.bagRounds }
            .average()
            .toInt()
    }

    private fun getWorkoutsThisWeek(
        workouts: List<Workout>
    ): List<Workout> {

        val calendar = Calendar.getInstance()

        calendar.firstDayOfWeek = Calendar.MONDAY

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfWeek = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_WEEK, 7)

        val startOfNextWeek = calendar.timeInMillis

        return workouts.filter { workout ->
            workout.date >= startOfWeek &&
                    workout.date < startOfNextWeek
        }
    }

    private fun getTrainedDays(
        workouts: List<Workout>
    ): Set<Int> {

        return workouts.map { workout ->

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = workout.date

            calendar.get(Calendar.DAY_OF_WEEK)

        }.toSet()
    }
}