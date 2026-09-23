package com.example.thecorner.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.thecorner.model.UserProfile
import com.example.thecorner.model.ProfileDefaults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "profile_preferences")

class ProfileDataStore(private val context: Context) {
    private object Keys {
        val name = androidx.datastore.preferences.core.stringPreferencesKey("name")
        val age = intPreferencesKey("age")
        val weightKg = floatPreferencesKey("weight_kg")
        val heightCm = intPreferencesKey("height_cm")
    }

    val profile: Flow<UserProfile> = context.profileDataStore.data.map { preferences ->
        UserProfile(
            name = preferences[Keys.name] ?: ProfileDefaults.DEFAULT_NAME,
            age = preferences[Keys.age],
            weightKg = preferences[Keys.weightKg],
            heightCm = preferences[Keys.heightCm]
        )
    }

    suspend fun save(profile: UserProfile) {
        context.profileDataStore.edit { preferences ->
            putOrRemove(preferences, Keys.name, profile.name.trim().ifEmpty { ProfileDefaults.DEFAULT_NAME })
            putOrRemove(preferences, Keys.age, profile.age)
            putOrRemove(preferences, Keys.weightKg, profile.weightKg)
            putOrRemove(preferences, Keys.heightCm, profile.heightCm)
        }
    }

    private fun <T> putOrRemove(preferences: MutablePreferences, key: Preferences.Key<T>, value: T?) {
        if (value == null) preferences.remove(key) else preferences[key] = value
    }
}
