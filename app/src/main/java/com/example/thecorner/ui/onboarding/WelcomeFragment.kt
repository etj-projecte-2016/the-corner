package com.example.thecorner.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.thecorner.R
import com.example.thecorner.databinding.FragmentWelcomeBinding
import com.example.thecorner.ui.applyTopAndBottomSystemBarInsets

class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.foregroundContent.applyTopAndBottomSystemBarInsets()
        binding.getStartedButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_profileSetup)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
