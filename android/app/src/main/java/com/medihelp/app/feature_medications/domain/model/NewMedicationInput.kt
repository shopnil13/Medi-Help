package com.medihelp.app.feature_medications.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class NewScheduleInput(
    val timeOfDay: LocalTime,
    val mealRelation: String,
    val doseAmount: String?,
)

data class NewMedicationInput(
    val name: String,
    val strength: String?,
    val dosageInstruction: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val schedules: List<NewScheduleInput>,
)
