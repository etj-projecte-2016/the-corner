package com.example.thecorner.data.repository

import android.content.Context
import com.example.thecorner.data.local.ProfileDataStore
import com.example.thecorner.model.UserProfile
import kotlinx.coroutines.flow.Flow

class ProfileRepository(context: Context) {
    private val dataStore = ProfileDataStore(context.applicationContext)

    val profile: Flow<UserProfile> = dataStore.profile

    suspend fun save(profile: UserProfile) = dataStore.save(profile)
}
