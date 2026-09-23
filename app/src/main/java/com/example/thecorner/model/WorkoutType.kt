package com.example.thecorner.model

enum class WorkoutType(val storageId: String) {
    BAG_WORK("bag_work"),
    PAD_WORK("pad_work"),
    SPARRING("sparring"),
    SHADOW_BOXING("shadow_boxing");

    companion object {
        fun fromStorageId(value: String?): WorkoutType? =
            entries.firstOrNull { it.storageId == value }
    }
}
