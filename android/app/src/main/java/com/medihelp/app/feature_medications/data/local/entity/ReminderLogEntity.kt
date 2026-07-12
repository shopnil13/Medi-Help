package com.medihelp.app.feature_medications.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminder_logs",
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
data class ReminderLogEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val scheduledAtEpochMillis: Long,
    val action: String,
    val actionAtEpochMillis: Long?,
    val isSynced: Boolean,
)
