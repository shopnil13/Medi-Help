package com.medihelp.app.feature_medications.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: String,
    val serverId: String?,
    val name: String,
    val strength: String?,
    val dosageInstruction: String,
    val simplifiedInstruction: String?,
    val purposeSimplified: String?,
    val startDateEpochDay: Long?,
    val endDateEpochDay: Long?,
    val status: String,
    val requiresReview: Boolean,
    val isSynced: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
