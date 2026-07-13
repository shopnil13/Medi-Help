package com.medihelp.app.feature_documents.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val documentType: String,
    val originalFilename: String,
    val contentType: String,
    val fileSizeBytes: Long,
    val documentStatus: String,
    val jobStatus: String,
    val progressPercent: Int,
    val errorMessage: String?,
    val structuredResultJson: String?,
    val confirmedResultJson: String?,
    val confirmedAtEpochMillis: Long?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
