package com.example.thecorner.ui.training.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.thecorner.R
import com.example.thecorner.databinding.FragmentEditWorkoutBinding
import com.example.thecorner.model.WorkoutConfig
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.training.WorkoutConfigContract

class EditWorkoutFragment : Fragment() {

    private var _binding: FragmentEditWorkoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditWorkoutViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditWorkoutBinding.inflate(
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

        viewModel.initialize(arguments)
        binding.workoutTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.typeBag -> WorkoutType.BAG_WORK
                R.id.typePad -> WorkoutType.PAD_WORK
                R.id.typeSparring -> WorkoutType.SPARRING
                R.id.typeShadow -> WorkoutType.SHADOW_BOXING
                else -> return@setOnCheckedChangeListener
            }
            if (viewModel.workoutConfig.value?.workoutType != type) viewModel.selectType(type)
        }
        setupToolbar()
        setupRoundDurationButtons()
        setupRestDurationButtons()
        setupRoundsButtons()
        setupSaveButton()

        observeWorkoutConfig()
    }


    // ============================================================
    // OBSERVER
    // ============================================================

    private fun observeWorkoutConfig() {

        viewModel.workoutConfig.observe(viewLifecycleOwner) { config ->

            updateUi(config)
        }
    }


    // ============================================================
    // TOOLBAR
    // ============================================================

    private fun setupToolbar() {

        binding.topAppBar.setNavigationOnClickListener {

            findNavController().navigateUp()
        }
    }


    // ============================================================
    // ROUND DURATION
    // ============================================================

    private fun setupRoundDurationButtons() {

        binding.btnRoundPlus.setOnClickListener {
            viewModel.increaseRoundDuration()
        }

        binding.btnRoundMinus.setOnClickListener {
            viewModel.decreaseRoundDuration()
        }
    }


    // ============================================================
    // REST DURATION
    // ============================================================

    private fun setupRestDurationButtons() {

        binding.btnRestPlus.setOnClickListener {
            viewModel.increaseRestDuration()
        }

        binding.btnRestMinus.setOnClickListener {
            viewModel.decreaseRestDuration()
        }
    }


    // ============================================================
    // ROUNDS
    // ============================================================

    private fun setupRoundsButtons() {

        binding.btnRoundsPlus.setOnClickListener {
            viewModel.increaseRounds()
        }

        binding.btnRoundsMinus.setOnClickListener {
            viewModel.decreaseRounds()
        }
    }


    // ============================================================
    // SAVE
    // ============================================================

    private fun setupSaveButton() {

        binding.btnSaveWorkoutSetup.setOnClickListener {

            val config =
                viewModel.workoutConfig.value
                    ?: return@setOnClickListener

            val result = WorkoutConfigContract.toBundle(config)

            parentFragmentManager.setFragmentResult(
                WorkoutConfigContract.RESULT,
                result
            )

            findNavController().navigateUp()
        }
    }


    // ============================================================
    // UI
    // ============================================================

    private fun updateUi(config: WorkoutConfig) {
        binding.workoutTypeGroup.check(when (config.workoutType) {
            WorkoutType.BAG_WORK -> R.id.typeBag
            WorkoutType.PAD_WORK -> R.id.typePad
            WorkoutType.SPARRING -> R.id.typeSparring
            WorkoutType.SHADOW_BOXING -> R.id.typeShadow
        })

        binding.tvEditRoundDuration.text =
            formatTime(config.roundDurationSeconds)

        binding.tvEditRestDuration.text =
            formatTime(config.restDurationSeconds)

        binding.tvEditRounds.text =
            config.numberOfRounds.toString()

        updateSummary(config)

        updateRoundPreview(config)
    }


    // ============================================================
    // SUMMARY
    // ============================================================

    private fun updateSummary(config: WorkoutConfig) {

        val totalRoundTime =
            config.roundDurationSeconds *
                    config.numberOfRounds

        val numberOfRests =
            (config.numberOfRounds - 1)
                .coerceAtLeast(0)

        val totalRestTime =
            config.restDurationSeconds *
                    numberOfRests

        val totalWorkoutSeconds =
            totalRoundTime +
                    totalRestTime

        binding.tvEditSummary.text =
            formatTime(totalWorkoutSeconds)
    }


    // ============================================================
    // PREVIEW
    // ============================================================

    private fun updateRoundPreview(config: WorkoutConfig) {

        binding.roundPreviewContainer.removeAllViews()

        repeat(config.numberOfRounds) { index ->

            val roundView = View(requireContext()).apply {

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    dpToPx(14),
                    config.roundDurationSeconds.toFloat()
                ).apply {
                    marginEnd = dpToPx(3)
                }

                setBackgroundResource(
                    R.drawable.bg_round_preview
                )
            }

            binding.roundPreviewContainer.addView(roundView)


            if (
                index < config.numberOfRounds - 1 &&
                config.restDurationSeconds > 0
            ) {

                val restView = View(requireContext()).apply {

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        dpToPx(14),
                        config.restDurationSeconds.toFloat()
                    ).apply {
                        marginEnd = dpToPx(3)
                    }

                    setBackgroundResource(
                        R.drawable.bg_rest_preview
                    )
                }

                binding.roundPreviewContainer.addView(restView)
            }
        }
    }


    // ============================================================
    // TIME
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


    private fun dpToPx(dp: Int): Int {

        return (
                dp *
                        resources.displayMetrics.density
                ).toInt()
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
