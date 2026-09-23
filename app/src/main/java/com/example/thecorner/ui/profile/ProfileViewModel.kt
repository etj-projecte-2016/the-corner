package com.example.thecorner.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.thecorner.TheCornerApplication

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as TheCornerApplication).appContainer.profileRepository
    val profile = repository.profile.asLiveData()
}
