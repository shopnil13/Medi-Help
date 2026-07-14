package com.medihelp.app.feature_medications.data.mapper

import com.medihelp.app.feature_medications.data.local.entity.MedicationEntity
import com.medihelp.app.feature_medications.data.local.entity.MedicationScheduleEntity
import com.medihelp.app.feature_medications.data.local.entity.MedicationWithSchedules
import com.medihelp.app.feature_medications.data.remote.dto.MedicationCreateRequestDto
import com.medihelp.app.feature_medications.data.remote.dto.MedicationResponseDto
import com.medihelp.app.feature_medications.data.remote.dto.MedicationScheduleRequestDto
import com.medihelp.app.feature_medications.data.remote.dto.MedicationScheduleResponseDto
import com.medihelp.app.feature_medications.domain.model.Medication
import com.medihelp.app.feature_medications.domain.model.MedicationSchedule
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import com.medihelp.app.feature_medications.domain.model.NewMedicationInput
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

fun MedicationWithSchedules.toDomain(): Medication = Medication(
    id = medication.id,
    name = medication.name,
    strength = medication.strength,
    dosageInstruction = medication.dosageInstruction,
    simplifiedInstruction = medication.simplifiedInstruction,
    purposeSimplified = medication.purposeSimplified,
    startDate = medication.startDateEpochDay?.let(LocalDate::ofEpochDay),
    endDate = medication.endDateEpochDay?.let(LocalDate::ofEpochDay),
    status = MedicationStatus.fromApiValue(medication.status),
    requiresReview = medication.requiresReview,
    isSynced = medication.isSynced,
    schedules = schedules.map { it.toDomain() },
)

fun MedicationScheduleEntity.toDomain(): MedicationSchedule = MedicationSchedule(
    id = id,
    timeOfDay = LocalTime.ofSecondOfDay(timeOfDayMinutes * 60L),
    frequencyType = frequencyType,
    mealRelation = mealRelation,
    doseAmount = doseAmount,
    notes = notes,
)

fun MedicationResponseDto.toEntity(localId: String, isSynced: Boolean): MedicationEntity {
    val now = System.currentTimeMillis()
    return MedicationEntity(
        id = localId,
        serverId = id,
        name = name,
        strength = strength,
        dosageInstruction = dosageInstruction,
        simplifiedInstruction = simplifiedInstruction,
        purposeSimplified = purposeSimplified,
        startDateEpochDay = startDate?.let { LocalDate.parse(it).toEpochDay() },
        endDateEpochDay = endDate?.let { LocalDate.parse(it).toEpochDay() },
        status = status,
        requiresReview = requiresReview,
        isSynced = isSynced,
        createdAtEpochMillis = now,
        updatedAtEpochMillis = now,
    )
}

fun MedicationResponseDto.toDomain(): Medication = Medication(
    id = id,
    name = name,
    strength = strength,
    dosageInstruction = dosageInstruction,
    simplifiedInstruction = simplifiedInstruction,
    purposeSimplified = purposeSimplified,
    startDate = startDate?.let(LocalDate::parse),
    endDate = endDate?.let(LocalDate::parse),
    status = MedicationStatus.fromApiValue(status),
    requiresReview = requiresReview,
    isSynced = true,
    schedules = schedules.map { schedule ->
        MedicationSchedule(
            id = schedule.id,
            timeOfDay = LocalTime.parse(schedule.timeOfDay, TIME_FORMATTER),
            frequencyType = schedule.frequencyType,
            mealRelation = schedule.mealRelation,
            doseAmount = schedule.doseAmount,
            notes = schedule.notes,
        )
    },
)

fun MedicationScheduleResponseDto.toEntity(localId: String, medicationLocalId: String) =
    MedicationScheduleEntity(
        id = localId,
        serverId = id,
        medicationId = medicationLocalId,
        timeOfDayMinutes = LocalTime.parse(timeOfDay, TIME_FORMATTER).toSecondOfDay() / 60,
        frequencyType = frequencyType,
        daysOfWeek = daysOfWeek,
        mealRelation = mealRelation,
        doseAmount = doseAmount,
        notes = notes,
    )

fun NewMedicationInput.toCreateRequestDto(): MedicationCreateRequestDto = MedicationCreateRequestDto(
    name = name,
    strength = strength,
    dosageInstruction = dosageInstruction,
    startDate = startDate?.toString(),
    endDate = endDate?.toString(),
    schedules = schedules.map { schedule ->
        MedicationScheduleRequestDto(
            timeOfDay = schedule.timeOfDay.format(TIME_FORMATTER),
            mealRelation = schedule.mealRelation,
            doseAmount = schedule.doseAmount,
        )
    },
)

fun newLocalId(): String = UUID.randomUUID().toString()
