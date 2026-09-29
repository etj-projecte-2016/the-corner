package com.example.thecorner.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.thecorner.R
import com.example.thecorner.databinding.FragmentProfileSetupBinding
import com.example.thecorner.model.UserProfile
import com.example.thecorner.model.ProfileField
import com.example.thecorner.model.ProfileValidator
import com.example.thecorner.ui.applyTopAndImeInsets

class ProfileSetupFragment : Fragment() {
    private var _binding: FragmentProfileSetupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileSetupViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.formContent.applyTopAndImeInsets()
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        listOf(binding.nameInput, binding.ageInput, binding.weightInput, binding.heightInput).forEach { input ->
            input.doAfterTextChanged { input.error = null }
        }
        binding.enterButton.setOnClickListener { complete() }
    }

    private fun complete() {
        val name = binding.nameInput.text.toString().trim()
        val age = binding.ageInput.text.toString().trim().toIntOrNull()
        val weight = binding.weightInput.text.toString().trim().toFloatOrNull()
        val height = binding.heightInput.text.toString().trim().toIntOrNull()
        val validation = ProfileValidator.validate(name, age, weight, height)
        binding.nameInput.error = if (ProfileField.NAME in validation.invalidFields) {
            getString(R.string.profile_name_error)
        } else null
        binding.ageInput.error = if (ProfileField.AGE in validation.invalidFields) {
            getString(R.string.profile_age_error)
        } else null
        binding.weightInput.error = if (ProfileField.WEIGHT in validation.invalidFields) {
            getString(R.string.profile_weight_error)
        } else null
        binding.heightInput.error = if (ProfileField.HEIGHT in validation.invalidFields) {
            getString(R.string.profile_height_error)
        } else null
        if (!validation.isValid) return

        binding.enterButton.isEnabled = false
        viewModel.completeOnboarding(UserProfile(name, age, weight, height)) {
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(binding.root.windowToken, 0)
            findNavController().navigate(
                R.id.homeFragment,
                null,
                navOptions {
                    popUpTo(R.id.welcomeFragment) { inclusive = true }
                    launchSingleTop = true
                }
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
