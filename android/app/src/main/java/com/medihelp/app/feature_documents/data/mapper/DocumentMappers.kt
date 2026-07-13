package com.medihelp.app.feature_documents.data.mapper

import com.medihelp.app.feature_documents.data.local.entity.DocumentEntity
import com.medihelp.app.feature_documents.data.remote.dto.ProcessingJobResponseDto
import com.medihelp.app.feature_documents.domain.model.UploadedDocument
import java.time.Instant

fun ProcessingJobResponseDto.toEntity() = DocumentEntity(
    id = document.id,
    jobId = id,
    documentType = document.documentType,
    originalFilename = document.originalFilename,
    contentType = document.contentType,
    fileSizeBytes = document.fileSizeBytes,
    documentStatus = document.status,
    jobStatus = status,
    progressPercent = progressPercent,
    errorMessage = errorMessage,
    createdAtEpochMillis = Instant.parse(createdAt).toEpochMilli(),
    updatedAtEpochMillis = Instant.parse(updatedAt).toEpochMilli(),
)

fun DocumentEntity.toDomain() = UploadedDocument(
    id = id,
    jobId = jobId,
    documentType = documentType,
    originalFilename = originalFilename,
    contentType = contentType,
    fileSizeBytes = fileSizeBytes,
    documentStatus = documentStatus,
    jobStatus = jobStatus,
    progressPercent = progressPercent,
    errorMessage = errorMessage,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)
