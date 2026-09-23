package com.example.thecorner.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.thecorner.R
import com.example.thecorner.databinding.FragmentHomeBinding
import com.example.thecorner.model.Workout
import com.example.thecorner.ui.training.labelRes
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding
        get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        observeUiState()
        setupBottomNavigationBehavior()
        binding.viewAllSessions.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }
    }

    private fun setupBottomNavigationBehavior() {

        val bottomNavigation =
            requireActivity().findViewById<View>(R.id.bottomNavigation)

        binding.root.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->

            if (scrollY > oldScrollY) {
                // Scroll hacia abajo → ocultamos BottomNavigation
                bottomNavigation.animate()
                    .translationY(bottomNavigation.height.toFloat())
                    .setDuration(200)
                    .start()
            } else if (scrollY < oldScrollY) {
                // Scroll hacia arriba → mostramos BottomNavigation
                bottomNavigation.animate()
                    .translationY(0f)
                    .setDuration(200)
                    .start()
            }
        }

    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.uiState.collect { uiState ->
                    render(uiState)
                }
            }
        }
    }

    private fun render(uiState: HomeUiState) {
        renderWeeklyStats(uiState)
        renderLastWorkout(uiState.lastWorkout)
        renderWeeklyActivity(uiState.trainedDays)
    }

    private fun renderWeeklyStats(uiState: HomeUiState) {
        binding.tvSessionsValue.text =
            uiState.workoutsThisWeek.toString()

        binding.tvDurationValue.text =
            (uiState.averageDuration / 60).toString()

        binding.tvCaloriesValue.text =
            getString(R.string.calories_est_value, uiState.averageCalories)

        binding.tvBagRoundsValue.text =
            uiState.averageBagRounds.toString()
    }

    private fun renderLastWorkout(workout: Workout?) {
        binding.cardLastSession.setOnClickListener(if (workout == null) null else View.OnClickListener {
            findNavController().navigate(R.id.action_home_to_details, Bundle().apply { putLong("sessionId", workout.id) })
        })
        binding.cardLastSession.isClickable = workout != null
        binding.cardLastSession.isFocusable = workout != null

        if (workout == null) {
            binding.tvLastSessionType.setText(
                R.string.home_no_session
            )

            binding.tvLastSessionDate.setText(
                R.string.home_start_first_workout
            )

            binding.tvLastSessionDuration.text = "-"
            binding.tvLastSessionCalories.text = "-"
            binding.tvLastSessionRounds.text = "-"

            return
        }

        binding.tvLastSessionType.setText(
            workout.workoutType?.labelRes() ?: R.string.home_boxing
        )

        binding.tvLastSessionDate.text =
            formatWorkoutDate(workout.date)

        binding.tvLastSessionDuration.text =
            (workout.duration / 60).toString()

        binding.tvLastSessionCalories.text =
            getString(R.string.calories_est_value, workout.calories)

        binding.tvLastSessionRounds.text =
            workout.totalRounds.toString()
    }

    private fun renderWeeklyActivity(
        trainedDays: Set<Int>
    ) {

        binding.dayMonday.setBackgroundResource(
            if (Calendar.MONDAY in trainedDays) {
                R.drawable.bg_day_active
            } else {
                R.drawable.bg_day_inactive
            }
        )

        binding.dayTuesday.setBackgroundResource(
            if (Calendar.TUESDAY in trainedDays) {
                R.drawable.bg_day_active
            } else {
                R.drawable.bg_day_inactive
            }
        )

        binding.dayWednesday.setBackgroundResource(
            if (Calendar.WEDNESDAY in trainedDays) {
                R.drawable.bg_day_active
            } else {
                R.drawable.bg_day_inactive
            }
        )

        binding.dayThursday.setBackgroundResource(
            if (Calendar.THURSDAY in trainedDays) {
                R.drawable.bg_day_active
            } else {
                R.drawable.bg_day_inactive
            }
        )

        binding.dayFriday.setBackgroundResource(
            if (Calendar.FRIDAY in trainedDays) {
                R.drawable.bg_day_active
            } else {
                R.drawable.bg_day_inactive
            }
        )

        binding.daySaturday.setBackgroundResource(
            if (Calendar.SATURDAY in trainedDays) {
                R.drawable.bg_day_active
            } else {
                R.drawable.bg_day_inactive
            }
        )

        binding.daySunday.setBackgroundResource(
            if (Calendar.SUNDAY in trainedDays) {
                R.drawable.bg_day_active
            } else {
                R.drawable.bg_day_inactive
            }
        )
    }

    private fun formatWorkoutDate(timestamp: Long): String {
        val workoutCalendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        val todayCalendar = Calendar.getInstance()

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = timeFormat.format(Date(timestamp))

        val isToday =
            workoutCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                    workoutCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            return getString(
                R.string.home_date_today,
                time
            )
        }

        todayCalendar.add(Calendar.DAY_OF_YEAR, -1)

        val isYesterday =
            workoutCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                    workoutCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return getString(
                R.string.home_date_yesterday,
                time
            )
        }

        val dateFormat = SimpleDateFormat(
            "d MMM",
            Locale.getDefault()
        )

        val date = dateFormat.format(Date(timestamp))

        return getString(
            R.string.home_date_regular,
            date,
            time
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
