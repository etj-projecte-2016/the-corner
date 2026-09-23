package com.example.thecorner.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thecorner.TheCornerApplication
import com.example.thecorner.model.UserProfile
import kotlinx.coroutines.launch

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as TheCornerApplication).appContainer.profileRepository

    fun save(profile: UserProfile, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.save(profile)
            onSaved()
        }
    }
}
