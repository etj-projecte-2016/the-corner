package com.example.thecorner.ui.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private var roundDurationSeconds = 180
    private var restDurationSeconds = 60
    private var numberOfRounds = 10


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

        updateWorkoutUi()
    }


    // ============================================================
    // EDIT WORKOUT RESULT
    // ============================================================

    private fun setupWorkoutConfigResult() {

        parentFragmentManager.setFragmentResultListener(
            WORKOUT_CONFIG_RESULT,
            viewLifecycleOwner
        ) { _, bundle ->

            roundDurationSeconds = bundle.getInt(
                KEY_ROUND_DURATION,
                roundDurationSeconds
            )

            restDurationSeconds = bundle.getInt(
                KEY_REST_DURATION,
                restDurationSeconds
            )

            numberOfRounds = bundle.getInt(
                KEY_NUMBER_OF_ROUNDS,
                numberOfRounds
            )

            updateWorkoutUi()
        }
    }


    // ============================================================
    // EDIT SESSION
    // ============================================================

    private fun setupEditSessionButton() {

        binding.btnEditSession.setOnClickListener {

            findNavController().navigate(
                R.id.action_trainingFragment_to_editWorkoutFragment
            )
        }
    }


    // ============================================================
    // START WORKOUT
    // ============================================================

    private fun setupStartWorkoutButton() {

        binding.btnStartWorkout.setOnClickListener {

            val bundle = Bundle().apply {

                putInt(
                    "roundDurationSeconds",
                    roundDurationSeconds
                )

                putInt(
                    "restDurationSeconds",
                    restDurationSeconds
                )

                putInt(
                    "numberOfRounds",
                    numberOfRounds
                )
            }

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

        binding.tvRoundDuration.text =
            formatTime(roundDurationSeconds)

        binding.tvRestDuration.text =
            formatTime(restDurationSeconds)

        binding.tvRounds.text =
            numberOfRounds.toString()

        updateEstimatedDuration()
    }


    // ============================================================
    // ESTIMATED DURATION
    // ============================================================

    private fun updateEstimatedDuration() {

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
            roundDurationSeconds * numberOfRounds

        val numberOfRests =
            (numberOfRounds - 1)
                .coerceAtLeast(0)

        val totalRestSeconds =
            restDurationSeconds * numberOfRests

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


    // ============================================================
    // RESULT KEYS
    // ============================================================

    companion object {

        private const val WORKOUT_CONFIG_RESULT =
            "workoutConfigResult"

        private const val KEY_ROUND_DURATION =
            "roundDurationSeconds"

        private const val KEY_REST_DURATION =
            "restDurationSeconds"

        private const val KEY_NUMBER_OF_ROUNDS =
            "numberOfRounds"
    }
}