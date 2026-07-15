package com.medihelp.app.feature_vitals.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vital_records",
    indices = [
        Index("metricType"),
        Index("recordedAtEpochMillis"),
        Index("source"),
        Index(value = ["serverId"], unique = true),
    ],
)
data class VitalRecordEntity(
    @PrimaryKey val id: String,
    val serverId: String?,
    val metricType: String,
    val metricName: String,
    val valueNumeric: Double,
    val unit: String,
    val recordedAtEpochMillis: Long,
    val source: String,
    val sourceDocumentId: String?,
    val notes: String?,
    val isSynced: Boolean,
    val createdAtEpochMillis: Long,
)

