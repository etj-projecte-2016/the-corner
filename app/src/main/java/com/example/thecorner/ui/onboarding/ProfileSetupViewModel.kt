package com.example.thecorner.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.model.UserProfile
import kotlinx.coroutines.launch

class ProfileSetupViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as TheCornerApplication).appContainer.profileRepository

    fun completeOnboarding(profile: UserProfile, onCompleted: () -> Unit) {
        viewModelScope.launch {
            repository.completeOnboarding(profile)
            onCompleted()
        }
    }
}
