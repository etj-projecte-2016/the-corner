package com.example.thecorner.ui.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.thecorner.R
import com.example.thecorner.databinding.FragmentTrainingBinding

class TrainingFragment : Fragment() {

    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!


    // ============================================================
    // CURRENT WORKOUT CONFIG
    // ============================================================

    private val viewModel: TrainingViewModel by viewModels()




    // ============================================================
    // FRAGMENT
    // ============================================================

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

        setupWorkoutConfigResult()
        setupEditSessionButton()
        setupStartWorkoutButton()

        viewModel.config.observe(viewLifecycleOwner) { updateWorkoutUi() }
    }


    // ============================================================
    // EDIT WORKOUT RESULT
    // ============================================================

    private fun setupWorkoutConfigResult() {

        parentFragmentManager.setFragmentResultListener(
            WorkoutConfigContract.RESULT,
            viewLifecycleOwner
        ) { _, bundle ->

            WorkoutConfigContract.fromBundle(bundle)?.let(viewModel::setConfig)
        }
    }


    // ============================================================
    // EDIT SESSION
    // ============================================================

    private fun setupEditSessionButton() {

        binding.btnEditSession.setOnClickListener {

            findNavController().navigate(
                R.id.action_trainingFragment_to_editWorkoutFragment,
                WorkoutConfigContract.toBundle(viewModel.config.value ?: return@setOnClickListener)
            )
        }
    }


    // ============================================================
    // START WORKOUT
    // ============================================================

    private fun setupStartWorkoutButton() {

        binding.btnStartWorkout.setOnClickListener {

            val bundle = WorkoutConfigContract.toBundle(viewModel.config.value ?: return@setOnClickListener)

            findNavController().navigate(
                R.id.action_trainingFragment_to_workoutSessionFragment,
                bundle
            )
        }
    }


    // ============================================================
    // UPDATE UI
    // ============================================================

    private fun updateWorkoutUi() {
        val config = viewModel.config.value ?: return
        binding.tvWorkoutType.setText(config.workoutType.labelRes())

        binding.tvRoundDuration.text =
            formatTime(config.roundDurationSeconds)

        binding.tvRestDuration.text =
            formatTime(config.restDurationSeconds)

        binding.tvRounds.text =
            config.numberOfRounds.toString()

        updateEstimatedDuration()
    }


    // ============================================================
    // ESTIMATED DURATION
    // ============================================================

    private fun updateEstimatedDuration() {
        val config = viewModel.config.value ?: return

        /*
         * Ejemplo:
         *
         * 10 rounds × 3:00 = 30:00
         *
         * Solo hay descanso ENTRE rounds:
         *
         * 9 descansos × 1:00 = 9:00
         *
         * Total = 39:00
         */

        val totalRoundSeconds =
            config.roundDurationSeconds * config.numberOfRounds

        val numberOfRests =
            (config.numberOfRounds - 1)
                .coerceAtLeast(0)

        val totalRestSeconds =
            config.restDurationSeconds * numberOfRests

        val totalDurationSeconds =
            totalRoundSeconds + totalRestSeconds

        binding.tvEstimatedDuration.text =
            formatTime(totalDurationSeconds)
    }


    // ============================================================
    // FORMAT TIME
    // ============================================================

    private fun formatTime(totalSeconds: Int): String {

        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format(
            "%d:%02d",
            minutes,
            seconds
        )
    }


    // ============================================================
    // DESTROY VIEW
    // ============================================================

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}