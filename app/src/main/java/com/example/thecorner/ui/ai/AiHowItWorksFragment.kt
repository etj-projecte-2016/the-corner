package com.example.thecorner.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.thecorner.ui.applyTopSystemBarInset
import com.example.thecorner.databinding.FragmentAiHowItWorksBinding

class AiHowItWorksFragment : Fragment() {
    private var _binding: FragmentAiHowItWorksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAiHowItWorksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.aiHowToolbar.applyTopSystemBarInset()
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
