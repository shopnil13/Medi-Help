package com.medihelp.app.feature_medications.domain.model

import java.time.LocalDate
import java.time.LocalTime

enum class MedicationStatus {
    ACTIVE, PAUSED, COMPLETED, CANCELLED;

    companion object {
        fun fromApiValue(value: String): MedicationStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}

data class MedicationSchedule(
    val id: String,
    val timeOfDay: LocalTime,
    val frequencyType: String,
    val mealRelation: String,
    val doseAmount: String?,
    val notes: String?,
)

data class Medication(
    val id: String,
    val name: String,
    val strength: String?,
    val dosageInstruction: String,
    val simplifiedInstruction: String?,
    val purposeSimplified: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val status: MedicationStatus,
    val requiresReview: Boolean,
    val isSynced: Boolean,
    val schedules: List<MedicationSchedule>,
)
