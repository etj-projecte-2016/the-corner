package com.example.thecorner.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.example.thecorner.R
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.databinding.FragmentSessionDetailsBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat

class SessionDetailsFragment : Fragment() {
    private var _binding: FragmentSessionDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SessionDetailsViewModel by viewModels {
        viewModelFactory { initializer {
            SessionDetailsViewModel((requireActivity().application as TheCornerApplication).appContainer.workoutRepository,
                requireArguments().getLong("sessionId"))
        } }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSessionDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.back.setOnClickListener { findNavController().navigateUp() }
        binding.retry.setOnClickListener { viewModel.retry() }
        requireActivity().findViewById<View>(R.id.bottomNavigation).apply {
            animate().cancel()
            translationY = 0f
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.loading.isVisible = state.isLoading
                    binding.content.isVisible = state.session != null
                    binding.message.isVisible = !state.isLoading && state.session == null
                    binding.retry.isVisible = state.hasError
                    binding.messageTitle.setText(if (state.hasError) R.string.history_error_title else R.string.history_missing_title)
                    binding.messageBody.setText(if (state.hasError) R.string.history_error_body else R.string.history_missing_body)
                    state.session?.let { session ->
                        binding.workoutType.text = session.displayTitle(requireContext())
                        binding.artwork.setImageResource(session.workoutType.historyImage())
                        binding.date.text = sessionDate(session.date)
                        binding.time.text = sessionTime(session.date)
                        binding.rounds.text = NumberFormat.getIntegerInstance().format(session.totalRounds)
                        binding.duration.text = sessionDuration(requireContext(), session.duration)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
