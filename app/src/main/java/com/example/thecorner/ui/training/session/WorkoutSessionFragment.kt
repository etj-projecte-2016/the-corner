package com.example.thecorner.ui.training.session

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.thecorner.R
import com.example.thecorner.ui.training.WorkoutConfigContract
import com.example.thecorner.databinding.FragmentWorkoutSessionBinding

class WorkoutSessionFragment : Fragment() {

    private var _binding: FragmentWorkoutSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkoutSessionViewModel by viewModels()

    private var lastCountdownNumber: Int? = null
    private var fightAnimated = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWorkoutSessionBinding.inflate(
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

        val config = WorkoutConfigContract.fromBundle(arguments)
        if (config == null) {
            findNavController().navigateUp()
            return
        }

        setupButtons()
        observeState()

        viewModel.configure(
            roundDurationSeconds = config.roundDurationSeconds,
            restDurationSeconds = config.restDurationSeconds,
            totalRounds = config.numberOfRounds,
            workoutType = config.workoutType
        )
    }


    private fun setupButtons() {

        binding.btnPauseWorkout.setOnClickListener {
            viewModel.togglePause()
        }

        binding.btnEndWorkout.setOnClickListener {
            viewModel.finishWorkout()
        }
    }


    private fun observeState() {

        viewModel.state.observe(
            viewLifecycleOwner
        ) { state ->

            renderState(state)
        }
    }


    private fun renderState(
        state: WorkoutSessionViewModel.SessionState
    ) {

        when (state.phase) {

            WorkoutSessionViewModel.Phase.PREPARING ->
                renderPreparing(state)

            WorkoutSessionViewModel.Phase.FIGHT ->
                renderFight()

            WorkoutSessionViewModel.Phase.ROUND ->
                renderRound(state)

            WorkoutSessionViewModel.Phase.REST ->
                renderRest(state)

            WorkoutSessionViewModel.Phase.FINISHED ->
                renderFinished(state)
        }
    }


    // ============================================================
    // PREPARING
    // ============================================================

    private fun renderPreparing(
        state: WorkoutSessionViewModel.SessionState
    ) {

        binding.ivSessionBackground.visibility =
            View.GONE

        hideNormalSessionUi()

        binding.countdownContainer.visibility =
            View.VISIBLE

        binding.ivCountdownBoxer.visibility =
            View.VISIBLE

        binding.ivFightBag.visibility =
            View.GONE

        binding.tvCountdownReady.visibility =
            View.VISIBLE

        binding.countdownStepsContainer.visibility =
            View.VISIBLE

        binding.tvCountdownMain.visibility =
            View.VISIBLE

        binding.tvCountdownMain.text =
            state.remainingSeconds.toString()

        binding.tvCountdownMain.textSize =
            250f

        binding.tvCountdownMain.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_text_primary
            )
        )

        updateCountdownSteps(
            state.remainingSeconds
        )

        if (
            lastCountdownNumber !=
            state.remainingSeconds
        ) {

            lastCountdownNumber =
                state.remainingSeconds

            animateCountdownNumber()
        }
    }


    private fun animateCountdownNumber() {

        binding.tvCountdownMain.animate().cancel()

        binding.tvCountdownMain.alpha = 1f
        binding.tvCountdownMain.scaleX = 0.65f
        binding.tvCountdownMain.scaleY = 0.65f

        val scaleX =
            ObjectAnimator.ofFloat(
                binding.tvCountdownMain,
                View.SCALE_X,
                0.65f,
                1.08f,
                1f
            )

        val scaleY =
            ObjectAnimator.ofFloat(
                binding.tvCountdownMain,
                View.SCALE_Y,
                0.65f,
                1.08f,
                1f
            )

        AnimatorSet().apply {

            playTogether(
                scaleX,
                scaleY
            )

            duration =
                450

            interpolator =
                OvershootInterpolator(0.7f)

            start()
        }
    }


    private fun updateCountdownSteps(
        currentNumber: Int
    ) {

        val active =
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_red
            )

        val inactive =
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_text_secondary
            )

        binding.tvCountdownStep3.setTextColor(
            if (currentNumber == 3) active else inactive
        )

        binding.tvCountdownStep2.setTextColor(
            if (currentNumber == 2) active else inactive
        )

        binding.tvCountdownStep1.setTextColor(
            if (currentNumber == 1) active else inactive
        )

        binding.tvCountdownStep3.alpha =
            if (currentNumber == 3) 1f else 0.35f

        binding.tvCountdownStep2.alpha =
            if (currentNumber == 2) 1f else 0.35f

        binding.tvCountdownStep1.alpha =
            if (currentNumber == 1) 1f else 0.35f
    }


    // ============================================================
    // FIGHT
    // ============================================================

    private fun renderFight() {

        binding.ivSessionBackground.visibility =
            View.GONE

        lastCountdownNumber =
            null

        hideNormalSessionUi()

        binding.countdownContainer.visibility =
            View.VISIBLE

        binding.ivCountdownBoxer.visibility =
            View.GONE

        binding.ivFightBag.visibility =
            View.VISIBLE

        binding.tvCountdownReady.visibility =
            View.GONE

        binding.countdownStepsContainer.visibility =
            View.GONE

        binding.tvCountdownMain.visibility =
            View.VISIBLE

        binding.tvCountdownMain.text =
            "FIGHT!"

        binding.tvCountdownMain.textSize =
            150f

        binding.tvCountdownMain.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_red
            )
        )

        if (!fightAnimated) {

            fightAnimated =
                true

            animateFight()
        }
    }


    private fun animateFight() {

        binding.tvCountdownMain.animate().cancel()

        binding.tvCountdownMain.alpha =
            1f

        binding.tvCountdownMain.scaleX =
            0.58f

        binding.tvCountdownMain.scaleY =
            0.58f

        val scaleX =
            ObjectAnimator.ofFloat(
                binding.tvCountdownMain,
                View.SCALE_X,
                0.58f,
                1.10f,
                1f
            )

        val scaleY =
            ObjectAnimator.ofFloat(
                binding.tvCountdownMain,
                View.SCALE_Y,
                0.58f,
                1.10f,
                1f
            )

        AnimatorSet().apply {

            playTogether(
                scaleX,
                scaleY
            )

            duration =
                520

            interpolator =
                OvershootInterpolator(0.9f)

            start()
        }
    }


    // ============================================================
    // ROUND
    // ============================================================

    private fun renderRound(
        state: WorkoutSessionViewModel.SessionState
    ) {

        binding.countdownContainer.visibility =
            View.GONE

        /*
         * Ahora sí ocupa toda la pantalla porque
         * está fuera del sessionContent con padding.
         */
        binding.ivSessionBackground.visibility =
            View.VISIBLE

        fightAnimated =
            false

        restoreNormalSessionUi()

        setupRoundHeader(
            currentRound = state.currentRound,
            totalRounds = state.totalRounds
        )

        binding.tvSessionTimer.text =
            formatTime(
                state.remainingSeconds
            )

        binding.tvSessionTimer.textSize =
            150f

        binding.tvSessionPhase.text =
            "ROUND"

        binding.tvSessionPhase.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_red
            )
        )


        if (state.remainingSeconds <= 10) {

            binding.tvSessionTimer.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.corner_red
                )
            )

        } else {

            binding.tvSessionTimer.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.corner_text_primary
                )
            )
        }


        if (
            state.currentRound <
            state.totalRounds &&
            state.restDurationSeconds > 0
        ) {

            binding.tvNextLabel.visibility =
                View.VISIBLE

            binding.nextPhaseContainer.visibility =
                View.VISIBLE

            binding.tvNextPhaseName.text =
                "REST"

            binding.tvNextPhaseName.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.corner_rest
                )
            )

            binding.tvNextPhaseDuration.text =
                "· ${formatTime(state.restDurationSeconds)}"

        } else {

            binding.tvNextLabel.visibility =
                View.INVISIBLE

            binding.nextPhaseContainer.visibility =
                View.INVISIBLE
        }

        updatePauseButton(state)
        updateTimeline(state)
    }


    // ============================================================
    // REST
    // ============================================================

    private fun renderRest(
        state: WorkoutSessionViewModel.SessionState
    ) {

        binding.countdownContainer.visibility =
            View.GONE

        binding.ivSessionBackground.visibility =
            View.VISIBLE

        restoreNormalSessionUi()

        binding.tvRoundLabel.text =
            "ROUND"

        binding.tvCurrentRound.text =
            state.currentRound.toString()

        binding.tvTotalRounds.text =
            "COMPLETE"

        binding.tvCurrentRound.visibility =
            View.VISIBLE

        binding.tvTotalRounds.visibility =
            View.VISIBLE


        val restColor =
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_rest
            )


        binding.tvCurrentRound.setTextColor(
            restColor
        )


        binding.tvSessionTimer.text =
            formatTime(
                state.remainingSeconds
            )

        binding.tvSessionTimer.textSize =
            150f

        binding.tvSessionTimer.setTextColor(
            restColor
        )


        binding.tvSessionPhase.text =
            "REST"

        binding.tvSessionPhase.setTextColor(
            restColor
        )


        binding.tvNextLabel.visibility =
            View.VISIBLE

        binding.nextPhaseContainer.visibility =
            View.VISIBLE

        binding.tvNextPhaseName.text =
            "ROUND ${state.currentRound + 1}"

        binding.tvNextPhaseName.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_red
            )
        )

        binding.tvNextPhaseDuration.text =
            "· ${formatTime(state.roundDurationSeconds)}"

        updatePauseButton(state)
        updateTimeline(state)
    }


    // ============================================================
    // HEADER
    // ============================================================

    private fun setupRoundHeader(
        currentRound: Int,
        totalRounds: Int
    ) {

        binding.roundHeaderContainer.visibility =
            View.VISIBLE

        binding.tvRoundLabel.visibility =
            View.VISIBLE

        binding.tvCurrentRound.visibility =
            View.VISIBLE

        binding.tvTotalRounds.visibility =
            View.VISIBLE

        binding.tvRoundLabel.text =
            "ROUND"

        binding.tvRoundLabel.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_text_primary
            )
        )

        binding.tvCurrentRound.text =
            currentRound.toString()

        binding.tvCurrentRound.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_red
            )
        )

        binding.tvTotalRounds.text =
            "/ $totalRounds"

        binding.tvTotalRounds.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_text_primary
            )
        )
    }


    // ============================================================
    // PAUSE
    // ============================================================

    private fun updatePauseButton(
        state: WorkoutSessionViewModel.SessionState
    ) {

        if (state.isPaused) {

            binding.tvPaused.visibility =
                View.VISIBLE

            binding.btnPauseWorkout.text =
                "RESUME"

            binding.btnPauseWorkout.setIconResource(
                R.drawable.ic_play
            )

        } else {

            binding.tvPaused.visibility =
                View.GONE

            binding.btnPauseWorkout.text =
                "PAUSE"

            binding.btnPauseWorkout.setIconResource(
                R.drawable.ic_pause
            )
        }
    }


    // ============================================================
    // TIMELINE
    // ============================================================

    private fun updateTimeline(
        state: WorkoutSessionViewModel.SessionState
    ) {

        binding.roundTimelineContainer.removeAllViews()

        repeat(state.totalRounds) { index ->

            val roundNumber =
                index + 1

            binding.roundTimelineContainer.addView(
                createTimelineItem(
                    roundNumber,
                    state
                )
            )
        }
    }


    private fun createTimelineItem(
        roundNumber: Int,
        state: WorkoutSessionViewModel.SessionState
    ): View {

        val isCurrent =
            roundNumber == state.currentRound

        val container =
            LinearLayout(requireContext()).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        dpToPx(86),
                        1f
                    )
            }


        val indicator =
            View(requireContext()).apply {

                layoutParams =
                    LinearLayout.LayoutParams(
                        dpToPx(30),
                        dpToPx(13)
                    )

                background =
                    createTimelineBackground(
                        getTimelineColor(
                            roundNumber,
                            state
                        )
                    )
            }


        val number =
            TextView(requireContext()).apply {

                text =
                    roundNumber.toString()

                textSize =
                    14f

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dpToPx(5),
                    0,
                    0
                )

                setTextColor(
                    getTimelineNumberColor(
                        roundNumber,
                        state
                    )
                )
            }


        container.addView(indicator)
        container.addView(number)


        if (isCurrent) {

            val marker =
                TextView(requireContext()).apply {

                    text =
                        "▲"

                    textSize =
                        14f

                    gravity =
                        Gravity.CENTER

                    setPadding(
                        0,
                        dpToPx(2),
                        0,
                        0
                    )

                    setTextColor(
                        getCurrentPhaseColor(
                            state
                        )
                    )
                }

            container.addView(marker)
        }


        return container
    }


    private fun getTimelineColor(
        roundNumber: Int,
        state: WorkoutSessionViewModel.SessionState
    ): Int {

        return when {

            roundNumber <
                    state.currentRound -> {

                ContextCompat.getColor(
                    requireContext(),
                    R.color.corner_red
                )
            }

            roundNumber ==
                    state.currentRound &&
                    state.phase ==
                    WorkoutSessionViewModel.Phase.ROUND -> {

                ContextCompat.getColor(
                    requireContext(),
                    R.color.corner_red
                )
            }

            roundNumber ==
                    state.currentRound &&
                    state.phase ==
                    WorkoutSessionViewModel.Phase.REST -> {

                ContextCompat.getColor(
                    requireContext(),
                    R.color.corner_rest
                )
            }

            else -> {

                ContextCompat.getColor(
                    requireContext(),
                    R.color.corner_surface
                )
            }
        }
    }


    private fun getTimelineNumberColor(
        roundNumber: Int,
        state: WorkoutSessionViewModel.SessionState
    ): Int {

        return if (
            roundNumber ==
            state.currentRound
        ) {

            getCurrentPhaseColor(
                state
            )

        } else {

            ContextCompat.getColor(
                requireContext(),
                R.color.corner_text_secondary
            )
        }
    }


    private fun getCurrentPhaseColor(
        state: WorkoutSessionViewModel.SessionState
    ): Int {

        return if (
            state.phase ==
            WorkoutSessionViewModel.Phase.REST
        ) {

            ContextCompat.getColor(
                requireContext(),
                R.color.corner_rest
            )

        } else {

            ContextCompat.getColor(
                requireContext(),
                R.color.corner_red
            )
        }
    }


    private fun createTimelineBackground(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            shape =
                GradientDrawable.RECTANGLE

            cornerRadius =
                dpToPx(2).toFloat()

            setColor(color)

            setStroke(
                dpToPx(1),
                ContextCompat.getColor(
                    requireContext(),
                    R.color.corner_outline
                )
            )
        }
    }


    // ============================================================
    // FINISHED
    // ============================================================

    private fun renderFinished(
        state: WorkoutSessionViewModel.SessionState
    ) {

        binding.countdownContainer.visibility =
            View.GONE

        binding.ivSessionBackground.visibility =
            View.GONE

        restoreNormalSessionUi()

        binding.tvRoundLabel.setText(
            if (state.saveStatus == WorkoutSessionViewModel.SaveStatus.NOT_REQUIRED) {
                R.string.session_ended
            } else {
                R.string.session_complete
            }
        )

        binding.tvCurrentRound.visibility =
            View.GONE

        binding.tvTotalRounds.visibility =
            View.GONE

        binding.tvSessionTimer.text =
            "✓"

        binding.tvSessionTimer.textSize =
            120f

        binding.tvSessionTimer.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_red
            )
        )

        binding.tvSessionPhase.text = when (state.saveStatus) {
            WorkoutSessionViewModel.SaveStatus.NOT_REQUIRED -> getString(R.string.session_not_saved)
            WorkoutSessionViewModel.SaveStatus.SAVING -> getString(R.string.session_saving)
            WorkoutSessionViewModel.SaveStatus.FAILED -> getString(R.string.session_save_failed)
            WorkoutSessionViewModel.SaveStatus.SAVED -> getString(R.string.session_rounds, state.totalRounds)
        }

        binding.tvSessionPhase.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.corner_red
            )
        )

        binding.roundTimelineContainer.visibility =
            View.GONE

        binding.tvNextLabel.visibility =
            View.GONE

        binding.nextPhaseContainer.visibility =
            View.GONE

        binding.tvPaused.visibility =
            View.GONE

        binding.btnPauseWorkout.visibility =
            View.GONE

        binding.btnEndWorkout.visibility =
            View.VISIBLE

        binding.btnEndWorkout.isEnabled =
            state.saveStatus != WorkoutSessionViewModel.SaveStatus.SAVING
        binding.btnEndWorkout.setText(
            when (state.saveStatus) {
                WorkoutSessionViewModel.SaveStatus.SAVING -> R.string.session_saving
                WorkoutSessionViewModel.SaveStatus.FAILED -> R.string.session_retry
                else -> R.string.session_finish
            }
        )

        binding.btnEndWorkout.setOnClickListener {
            when (viewModel.state.value?.saveStatus) {
                WorkoutSessionViewModel.SaveStatus.FAILED -> viewModel.retrySave()
                WorkoutSessionViewModel.SaveStatus.SAVED,
                WorkoutSessionViewModel.SaveStatus.NOT_REQUIRED -> findNavController().navigateUp()
                else -> Unit
            }
        }
    }


    // ============================================================
    // VISIBILITY
    // ============================================================

    private fun hideNormalSessionUi() {

        binding.roundHeaderContainer.visibility =
            View.GONE

        binding.centerTimerArea.visibility =
            View.GONE

        binding.tvPaused.visibility =
            View.GONE

        binding.btnPauseWorkout.visibility =
            View.GONE

        binding.btnEndWorkout.visibility =
            View.GONE
    }


    private fun restoreNormalSessionUi() {

        binding.roundHeaderContainer.visibility =
            View.VISIBLE

        binding.centerTimerArea.visibility =
            View.VISIBLE

        binding.tvSessionTimer.visibility =
            View.VISIBLE

        binding.tvSessionPhase.visibility =
            View.VISIBLE

        binding.roundTimelineContainer.visibility =
            View.VISIBLE

        binding.btnPauseWorkout.visibility =
            View.VISIBLE

        binding.btnEndWorkout.visibility =
            View.VISIBLE
    }


    // ============================================================
    // HELPERS
    // ============================================================

    private fun formatTime(
        totalSeconds: Int
    ): String {

        val minutes =
            totalSeconds / 60

        val seconds =
            totalSeconds % 60

        return String.format(
            "%d:%02d",
            minutes,
            seconds
        )
    }


    private fun dpToPx(
        dp: Int
    ): Int {

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
