package com.example.thecorner.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.thecorner.R
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.databinding.FragmentAiBinding
import kotlinx.coroutines.launch

class AiFragment : Fragment() {
    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AiViewModel by viewModels {
        val application = requireContext().applicationContext as TheCornerApplication
        AiViewModelFactory(application.appContainer.aiService)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.checkGeminiButton.setOnClickListener { viewModel.runConnectivityTest() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: AiUiState) {
        binding.statusText.text = when (state) {
            AiUiState.Idle -> getString(R.string.ai_status_ready)
            AiUiState.Loading -> getString(R.string.ai_status_generating)
            is AiUiState.Success -> getString(R.string.ai_status_ready)
            is AiUiState.Error -> getString(R.string.ai_status_error)
        }
        binding.resultText.text = when (state) {
            AiUiState.Idle, AiUiState.Loading -> ""
            is AiUiState.Success -> state.text
            is AiUiState.Error -> when (state.error) {
                AIError.Network -> getString(R.string.ai_error_network)
                AIError.Timeout -> getString(R.string.ai_error_timeout)
                AIError.RateLimited -> getString(R.string.ai_error_rate_limited)
                AIError.ServiceUnavailable -> getString(R.string.ai_error_service_unavailable)
                AIError.Authentication -> getString(R.string.ai_error_configuration)
                AIError.Safety -> getString(R.string.ai_error_safety)
                AIError.Unknown -> getString(R.string.ai_error_unknown)
            }
        }
        binding.checkGeminiButton.isEnabled = state !is AiUiState.Loading
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
