package com.example.thecorner.model

enum class ProfileField {
    NAME,
    AGE,
    WEIGHT,
    HEIGHT,
}

data class ProfileValidationResult(
    val invalidFields: Set<ProfileField>,
) {
    val isValid: Boolean get() = invalidFields.isEmpty()
}

/** Single source of truth for profile form rules used by onboarding and Profile Edit. */
object ProfileValidator {
    const val MIN_AGE = 10
    const val MAX_AGE = 100
    const val MIN_WEIGHT_KG = 30f
    const val MAX_WEIGHT_KG = 250f
    const val MIN_HEIGHT_CM = 120
    const val MAX_HEIGHT_CM = 230
    const val MAX_NAME_LENGTH = 40

    fun validate(
        name: String,
        age: Int?,
        weightKg: Float?,
        heightCm: Int?,
    ): ProfileValidationResult {
        val invalidFields = buildSet {
            if (name.trim().isBlank() || name.trim().length > MAX_NAME_LENGTH) add(ProfileField.NAME)
            if (age !in MIN_AGE..MAX_AGE) add(ProfileField.AGE)
            if (weightKg == null || !weightKg.isFinite() || weightKg !in MIN_WEIGHT_KG..MAX_WEIGHT_KG) {
                add(ProfileField.WEIGHT)
            }
            if (heightCm !in MIN_HEIGHT_CM..MAX_HEIGHT_CM) add(ProfileField.HEIGHT)
        }
        return ProfileValidationResult(invalidFields)
    }
}
