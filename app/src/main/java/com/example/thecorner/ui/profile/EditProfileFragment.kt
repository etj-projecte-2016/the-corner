package com.example.thecorner.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.content.Context
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.thecorner.R
import com.example.thecorner.databinding.FragmentEditProfileBinding
import com.example.thecorner.model.UserProfile

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            if (!binding.nameInput.hasFocus()) binding.nameInput.setText(profile.name)
            if (!binding.ageInput.hasFocus()) binding.ageInput.setText(profile.age?.toString().orEmpty())
            if (!binding.weightInput.hasFocus()) binding.weightInput.setText(profile.weightKg?.let { formatWeight(it) }.orEmpty())
            if (!binding.heightInput.hasFocus()) binding.heightInput.setText(profile.heightCm?.toString().orEmpty())
        }
        binding.saveButton.setOnClickListener { save() }
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        listOf(binding.nameInput, binding.ageInput, binding.weightInput, binding.heightInput).forEach { input ->
            input.doAfterTextChanged { input.error = null }
        }
    }

    private fun save() {
        val name = binding.nameInput.text.toString().trim()
        val age = binding.ageInput.text.toString().trim().toIntOrNull()
        val weight = binding.weightInput.text.toString().trim().toFloatOrNull()
        val height = binding.heightInput.text.toString().trim().toIntOrNull()
        var valid = true
        if (name.isBlank() || name.length > 40) { binding.nameInput.error = getString(R.string.profile_name_error); valid = false }
        if (age == null || age !in 10..100) { binding.ageInput.error = getString(R.string.profile_age_error); valid = false }
        if (weight == null || weight !in 30f..250f) { binding.weightInput.error = getString(R.string.profile_weight_error); valid = false }
        if (height == null || height !in 120..230) { binding.heightInput.error = getString(R.string.profile_height_error); valid = false }
        if (!valid) return

        viewModel.save(UserProfile(name, age, weight, height)) {
            binding.root.findFocus()?.clearFocus()
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(binding.root.windowToken, 0)
            findNavController().navigateUp()
        }
    }

    private fun formatWeight(weight: Float): String =
        if (weight % 1f == 0f) weight.toInt().toString() else weight.toString()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
