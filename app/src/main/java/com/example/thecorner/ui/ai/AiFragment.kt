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
import com.example.thecorner.databinding.FragmentAiBinding
import kotlinx.coroutines.launch

class AiFragment : Fragment() {
    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AiViewModel by viewModels()

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
        binding.statusText.text = statusText(state)
        binding.resultText.text = state.result.ifBlank { state.error.orEmpty() }
        binding.checkGeminiButton.isEnabled = state.status != AiStatus.GENERATING
    }

    private fun statusText(state: AiUiState): String = when (state.status) {
        AiStatus.READY, AiStatus.SUCCESS -> getString(R.string.ai_status_ready)
        AiStatus.GENERATING -> getString(R.string.ai_status_generating)
        AiStatus.ERROR -> getString(R.string.ai_status_error)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
