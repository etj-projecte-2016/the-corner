package com.example.thecorner.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.thecorner.R
import com.example.thecorner.databinding.FragmentProfileBinding
import com.example.thecorner.model.UserProfile
import com.example.thecorner.BuildConfig
import com.example.thecorner.ui.addTopInsetToLayoutMargin

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvBrand.addTopInsetToLayoutMargin()
        viewModel.profile.observe(viewLifecycleOwner) { render(it) }
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_edit_profile)
        }
        setupDeveloperTools()
    }

    private fun setupDeveloperTools() {
        binding.developerTools.isVisible = BuildConfig.DEBUG
        if (!BuildConfig.DEBUG) return

        binding.btnDebugSeedWorkouts.setOnClickListener { invokeDebugSeeder("seed") }
        binding.btnDebugClearWorkouts.setOnClickListener { invokeDebugSeeder("clear") }
    }

    private fun invokeDebugSeeder(methodName: String) {
        if (!BuildConfig.DEBUG) return
        try {
            val seeder = Class.forName("com.example.thecorner.debug.DebugWorkoutSeeder")
                .getField("INSTANCE").get(null)
            seeder.javaClass.getMethod(methodName, android.content.Context::class.java)
                .invoke(seeder, requireContext())
        } catch (error: ReflectiveOperationException) {
            Toast.makeText(requireContext(), R.string.profile_debug_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun render(profile: UserProfile) {
        binding.tvProfileName.text = profile.name
        binding.tvAge.text = profile.age?.toString() ?: getString(R.string.profile_empty_value)
        binding.tvWeight.text = profile.weightKg?.let { formatWeight(it) } ?: getString(R.string.profile_empty_value)
        binding.tvHeight.text = profile.heightCm?.toString() ?: getString(R.string.profile_empty_value)
    }

    private fun formatWeight(weight: Float): String =
        if (weight % 1f == 0f) weight.toInt().toString() else weight.toString()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
