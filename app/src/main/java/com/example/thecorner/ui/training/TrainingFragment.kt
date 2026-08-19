package com.example.thecorner.ui.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.thecorner.R
import com.example.thecorner.databinding.FragmentTrainingBinding
import kotlinx.coroutines.launch

class TrainingFragment : Fragment() {

    private var _binding: FragmentTrainingBinding? = null

    private val binding
        get() = _binding!!

    private val viewModel: WorkoutSetupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTrainingBinding.inflate(
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

        binding.btnEditSession.setOnClickListener {
            findNavController().navigate(
                R.id.action_trainingFragment_to_editWorkoutFragment
            )
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

    private fun render(
        uiState: WorkoutSetupUiState
    ) {

        // Round duration
        binding.tvRoundDuration.text =
            formatDuration(uiState.roundDuration)

        // Rest duration
        binding.tvRestDuration.text =
            formatDuration(uiState.restDuration)

        // Number of rounds
        binding.tvRounds.text =
            uiState.rounds.toString()


        // Estimated total workout duration
        val estimatedDuration =
            (uiState.roundDuration * uiState.rounds) +
                    (uiState.restDuration * (uiState.rounds - 1))

        binding.tvEstimatedDuration.text =
            formatDuration(estimatedDuration)


        // Temporary estimation.
        // We will improve this calculation later.
        val estimatedCalories = 450

        binding.tvEstimatedCalories.text =
            getString(
                R.string.training_calories_format,
                estimatedCalories
            )
    }

    private fun formatDuration(
        seconds: Int
    ): String {

        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format(
            "%d:%02d",
            minutes,
            remainingSeconds
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}