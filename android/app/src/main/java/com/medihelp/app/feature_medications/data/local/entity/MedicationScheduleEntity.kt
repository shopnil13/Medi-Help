package com.medihelp.app.feature_medications.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("medicationId")],
)
data class MedicationScheduleEntity(
    @PrimaryKey val id: String,
    val serverId: String?,
    val medicationId: String,
    val timeOfDayMinutes: Int,
    val frequencyType: String,
    val daysOfWeek: String?,
    val mealRelation: String,
    val doseAmount: String?,
    val notes: String?,
)
