package com.medihelp.app.feature_medications.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MedicationScheduleRequestDto(
    @SerialName("time_of_day") val timeOfDay: String,
    @SerialName("frequency_type") val frequencyType: String = "daily",
    @SerialName("days_of_week") val daysOfWeek: String? = null,
    @SerialName("meal_relation") val mealRelation: String = "unknown",
    @SerialName("dose_amount") val doseAmount: String? = null,
    val notes: String? = null,
)

@Serializable
data class MedicationScheduleResponseDto(
    val id: String,
    @SerialName("medication_id") val medicationId: String,
    @SerialName("time_of_day") val timeOfDay: String,
    @SerialName("frequency_type") val frequencyType: String,
    @SerialName("days_of_week") val daysOfWeek: String? = null,
    @SerialName("meal_relation") val mealRelation: String,
    @SerialName("dose_amount") val doseAmount: String? = null,
    val notes: String? = null,
)

@Serializable
data class MedicationCreateRequestDto(
    val name: String,
    val strength: String? = null,
    @SerialName("dosage_instruction") val dosageInstruction: String,
    @SerialName("purpose_simplified") val purposeSimplified: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val schedules: List<MedicationScheduleRequestDto> = emptyList(),
)

@Serializable
data class MedicationUpdateRequestDto(
    val status: String? = null,
)

@Serializable
data class MedicationResponseDto(
    val id: String,
    val name: String,
    val strength: String? = null,
    @SerialName("dosage_instruction") val dosageInstruction: String,
    @SerialName("simplified_instruction") val simplifiedInstruction: String? = null,
    @SerialName("purpose_simplified") val purposeSimplified: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val status: String,
    @SerialName("requires_review") val requiresReview: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val schedules: List<MedicationScheduleResponseDto> = emptyList(),
)

@Serializable
data class ReminderLogRequestDto(
    @SerialName("medication_id") val medicationId: String,
    @SerialName("scheduled_at") val scheduledAt: String,
    val action: String,
    @SerialName("action_at") val actionAt: String? = null,
)
