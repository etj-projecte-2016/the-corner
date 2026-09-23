package com.example.thecorner.model

data class UserProfile(
    val name: String = ProfileDefaults.DEFAULT_NAME,
    val age: Int? = null,
    val weightKg: Float? = null,
    val heightCm: Int? = null
)

object ProfileDefaults {
    const val DEFAULT_NAME = "Boxer"
}
