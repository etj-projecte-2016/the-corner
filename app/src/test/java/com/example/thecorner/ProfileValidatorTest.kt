package com.example.thecorner

import com.example.thecorner.model.ProfileField
import com.example.thecorner.model.ProfileValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidatorTest {
    @Test
    fun acceptsSharedProfileRulesIncludingDecimalWeight() {
        val result = ProfileValidator.validate("Boxer", 30, 74.5f, 180)

        assertTrue(result.isValid)
    }

    @Test
    fun rejectsValuesOutsideSharedRanges() {
        val result = ProfileValidator.validate("", 9, 29.9f, 231)

        assertFalse(result.isValid)
        assertTrue(ProfileField.NAME in result.invalidFields)
        assertTrue(ProfileField.AGE in result.invalidFields)
        assertTrue(ProfileField.WEIGHT in result.invalidFields)
        assertTrue(ProfileField.HEIGHT in result.invalidFields)
    }
}
