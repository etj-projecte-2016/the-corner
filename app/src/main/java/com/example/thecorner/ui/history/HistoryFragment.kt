package com.example.thecorner.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thecorner.R
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.databinding.FragmentHistoryBinding
import com.example.thecorner.model.WorkoutType
import kotlinx.coroutines.launch
import java.text.NumberFormat
import kotlin.math.roundToLong

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels {
        viewModelFactory { initializer {
            HistoryViewModel((requireActivity().application as TheCornerApplication).appContainer.workoutRepository,
                createSavedStateHandle())
        } }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = HistoryAdapter { id ->
            findNavController().navigate(R.id.action_history_to_details, Bundle().apply { putLong("sessionId", id) })
        }
        binding.sessions.layoutManager = LinearLayoutManager(requireContext())
        binding.sessions.adapter = adapter
        binding.back.setOnClickListener { findNavController().navigateUp() }
        binding.retry.setOnClickListener { viewModel.retry() }
        // Restore selection before attaching the listener so recreation cannot reset the filter.
        binding.filters.check(filterId(viewModel.uiState.value.selectedFilter))
        // Only user clicks update state; rendering a checked chip must not feed stale state back.
        listOf(
            binding.filterAll to null,
            binding.filterBag to WorkoutType.BAG_WORK,
            binding.filterPads to WorkoutType.PAD_WORK,
            binding.filterShadow to WorkoutType.SHADOW_BOXING,
            binding.filterSparring to WorkoutType.SPARRING
        ).forEach { (chip, type) ->
            chip.setOnClickListener { viewModel.selectFilter(type) }
        }
        requireActivity().findViewById<View>(R.id.bottomNavigation).apply {
            animate().cancel()
            translationY = 0f
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.filters.check(filterId(state.selectedFilter))
                    binding.loading.isVisible = state.isLoading
                    binding.sessions.isVisible = !state.isLoading && !state.hasError && !state.isEmpty
                    binding.message.isVisible = state.isEmpty || state.hasError
                    binding.retry.isVisible = state.hasError
                    binding.summary.root.isVisible = !state.isLoading && !state.hasError
                    val summary = state.summary
                    binding.summary.sessionCount.text = NumberFormat.getIntegerInstance().format(summary.sessions)
                    binding.summary.totalTime.text = historyCompactDuration(requireContext(), summary.durationSeconds)
                    binding.summary.thisMonth.text = NumberFormat.getIntegerInstance().format(summary.sessionsThisMonth)
                    binding.summary.calorieColumn.isVisible = summary.estimatedCalories != null
                    binding.summary.calorieDivider.isVisible = summary.estimatedCalories != null
                    binding.summary.totalCalories.text = summary.estimatedCalories?.let {
                        getString(R.string.history_approximate_value, NumberFormat.getIntegerInstance().format(it.roundToLong()))
                    }
                    binding.messageTitle.setText(when {
                        state.hasError -> R.string.history_error_title
                        state.selectedFilter != null -> R.string.history_filter_empty_title
                        else -> R.string.history_empty_title
                    })
                    binding.messageBody.setText(when {
                        state.hasError -> R.string.history_error_body
                        state.selectedFilter != null -> R.string.history_filter_empty_body
                        else -> R.string.history_empty_body
                    })
                    if (!state.isLoading) adapter.submitMonths(state.months)
                }
            }
        }
    }

    private fun filterId(type: WorkoutType?): Int = when (type) {
        WorkoutType.BAG_WORK -> R.id.filterBag
        WorkoutType.PAD_WORK -> R.id.filterPads
        WorkoutType.SHADOW_BOXING -> R.id.filterShadow
        WorkoutType.SPARRING -> R.id.filterSparring
        null -> R.id.filterAll
    }

    override fun onDestroyView() {
        binding.sessions.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
