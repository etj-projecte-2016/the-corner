package com.example.thecorner.ui.ai

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.thecorner.R
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.databinding.FragmentAiBinding
import com.example.thecorner.model.Workout
import com.example.thecorner.model.WorkoutType
import com.example.thecorner.ui.applyTopSystemBarInset
import com.example.thecorner.ui.history.historyCardImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiFragment : Fragment() {
    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AiViewModel by activityViewModels {
        val application = requireContext().applicationContext as TheCornerApplication
        AiViewModelFactory(
            service = application.appContainer.aiService,
            workoutRepository = application.appContainer.workoutRepository,
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.aiToolbar.applyTopSystemBarInset()
        binding.checkGeminiButton.setOnClickListener { viewModel.analyzeLastSession() }
        binding.howItWorksCard.setOnClickListener {
            findNavController().navigate(R.id.action_aiFragment_to_aiHowItWorksFragment)
        }
        viewModel.onScreenVisible()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: AiUiState) {
        val success = state as? AiUiState.AnalysisSuccess
        val readyWorkout = when (state) {
            is AiUiState.Ready -> state.workout
            is AiUiState.Loading -> state.workout
            is AiUiState.Error -> state.workout
            else -> null
        }
        binding.sessionTypeText.visibility = if (success != null) View.VISIBLE else View.GONE
        binding.sessionDateText.visibility = if (success != null || readyWorkout != null) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.successContent.visibility = if (success == null) View.GONE else View.VISIBLE
        binding.readyContent.visibility = if (readyWorkout == null) View.GONE else View.VISIBLE
        binding.notConfiguredContent.visibility = if (state is AiUiState.NotConfigured) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.noWorkoutContent.visibility = if (state is AiUiState.NoWorkout) View.VISIBLE else View.GONE
        binding.statusText.visibility = if (state is AiUiState.Loading || state is AiUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            AiUiState.Idle -> binding.statusText.text = getString(R.string.ai_status_ready)
            AiUiState.NotConfigured -> Unit
            is AiUiState.Ready -> renderReady(state.workout, showAction = true)
            is AiUiState.Loading -> {
                renderReady(state.workout, showAction = false)
                binding.statusText.text = getString(R.string.ai_status_generating)
            }
            is AiUiState.Error -> {
                state.workout?.let { renderReady(it, showAction = true) }
                binding.statusText.text = errorMessage(state.error)
            }
            is AiUiState.AnalysisSuccess -> renderSuccess(state)
            AiUiState.NoWorkout -> Unit
        }
    }

    private fun renderReady(workout: Workout, showAction: Boolean) {
        val locale = Locale.getDefault()
        val formattedDate = SimpleDateFormat("dd MMM yyyy", locale)
            .format(Date(workout.date)).uppercase(locale)
        binding.sessionDateText.text = formattedDate
        binding.readyDateText.text = formattedDate
        binding.readyTypeText.text = workoutTypeLabel(workout.workoutType)
        binding.readyWorkoutTitle.text = workoutTypeLabel(workout.workoutType)
        binding.sessionArtwork.setImageResource(workout.workoutType.historyCardImage())
        binding.readyRoundsValue.text = workout.totalRounds.toString()
        binding.readyDurationValue.text = getString(R.string.ai_minutes_value, workout.duration / 60)
        binding.readyCaloriesValue.text = workout.calories.toString()
        binding.checkGeminiButton.visibility = if (showAction) View.VISIBLE else View.GONE
        binding.checkGeminiButton.isEnabled = showAction
    }

    private fun renderSuccess(state: AiUiState.AnalysisSuccess) {
        val workout = state.workout
        val analysis = state.analysis
        val locale = Locale.getDefault()

        binding.sessionDateText.text = SimpleDateFormat("dd MMM yyyy", locale)
            .format(Date(workout.date)).uppercase(locale)
        binding.sessionTypeText.text = workoutTypeLabel(workout.workoutType)
        binding.analysisHeadline.text = analysis.headline
        binding.coachTakeText.text = analysis.summary
        binding.nextFocusText.text = analysis.nextSessionFocus
        binding.root.findViewById<TextView>(R.id.roundsValue).text = workout.totalRounds.toString()
        binding.root.findViewById<TextView>(R.id.durationValue).text =
            getString(R.string.ai_minutes_value, workout.duration / 60)
        binding.root.findViewById<TextView>(R.id.caloriesValue).text = workout.calories.toString()

        renderBulletList(binding.positivesContainer, analysis.positives, R.drawable.ic_check, R.color.ai_positive)
        renderBulletList(
            binding.improvementsContainer,
            analysis.improvements,
            R.drawable.ic_ai_arrow_up_right,
            R.color.ai_improvement,
        )
    }

    private fun renderBulletList(container: LinearLayout, items: List<String>, icon: Int, color: Int) {
        container.removeAllViews()
        items.forEach { item ->
            val row = layoutInflater.inflate(R.layout.item_ai_bullet, container, false)
            row.findViewById<ImageView>(R.id.icon).apply {
                setImageResource(icon)
                imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
            }
            row.findViewById<TextView>(R.id.text).text = item
            container.addView(row)
        }
    }

    private fun workoutTypeLabel(type: WorkoutType?): String = when (type) {
        WorkoutType.BAG_WORK -> getString(R.string.workout_type_bag)
        WorkoutType.PAD_WORK -> getString(R.string.workout_type_pad)
        WorkoutType.SPARRING -> getString(R.string.workout_type_sparring)
        WorkoutType.SHADOW_BOXING -> getString(R.string.workout_type_shadow)
        null -> getString(R.string.home_boxing)
    }.uppercase(Locale.getDefault())

    private fun errorMessage(error: AIError): String = when (error) {
        AIError.NotConfigured -> getString(R.string.ai_error_not_configured)
        AIError.Network -> getString(R.string.ai_error_network)
        AIError.Timeout -> getString(R.string.ai_error_timeout)
        AIError.RateLimited -> getString(R.string.ai_error_rate_limited)
        AIError.ServiceUnavailable -> getString(R.string.ai_error_service_unavailable)
        AIError.Authentication -> getString(R.string.ai_error_configuration)
        AIError.Safety -> getString(R.string.ai_error_safety)
        AIError.NoWorkout -> getString(R.string.ai_error_no_workout)
        AIError.InvalidResponse -> getString(R.string.ai_error_invalid_response)
        AIError.Unknown -> getString(R.string.ai_error_unknown)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
