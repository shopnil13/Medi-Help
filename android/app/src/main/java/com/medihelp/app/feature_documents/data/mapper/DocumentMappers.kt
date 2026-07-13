package com.medihelp.app.feature_documents.data.mapper

import com.medihelp.app.feature_documents.data.local.entity.DocumentEntity
import com.medihelp.app.feature_documents.data.remote.dto.ProcessingJobResponseDto
import com.medihelp.app.feature_documents.data.remote.dto.ExtractedBiomarkerDto
import com.medihelp.app.feature_documents.data.remote.dto.ExtractedMedicationDto
import com.medihelp.app.feature_documents.data.remote.dto.ExtractionResultDto
import com.medihelp.app.feature_documents.data.remote.dto.LabReportExtractionDto
import com.medihelp.app.feature_documents.data.remote.dto.PrescriptionExtractionDto
import com.medihelp.app.feature_documents.domain.model.ExtractedBiomarker
import com.medihelp.app.feature_documents.domain.model.ExtractedMedication
import com.medihelp.app.feature_documents.domain.model.ExtractionResult
import com.medihelp.app.feature_documents.domain.model.LabReportExtraction
import com.medihelp.app.feature_documents.domain.model.PrescriptionExtraction
import com.medihelp.app.feature_documents.domain.model.UploadedDocument
import java.time.Instant
import kotlinx.serialization.json.Json

fun ProcessingJobResponseDto.toEntity(json: Json) = DocumentEntity(
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
    structuredResultJson = structuredResult?.let {
        json.encodeToString(ExtractionResultDto.serializer(), it)
    },
    confirmedResultJson = confirmedResult?.let {
        json.encodeToString(ExtractionResultDto.serializer(), it)
    },
    confirmedAtEpochMillis = confirmedAt?.let(Instant::parse)?.toEpochMilli(),
    createdAtEpochMillis = Instant.parse(createdAt).toEpochMilli(),
    updatedAtEpochMillis = Instant.parse(updatedAt).toEpochMilli(),
)

fun DocumentEntity.toDomain(json: Json) = UploadedDocument(
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
    structuredResult = structuredResultJson?.let {
        json.decodeFromString(ExtractionResultDto.serializer(), it).toDomain()
    },
    confirmedResult = confirmedResultJson?.let {
        json.decodeFromString(ExtractionResultDto.serializer(), it).toDomain()
    },
    confirmedAt = confirmedAtEpochMillis?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun ExtractionResultDto.toDomain(): ExtractionResult = when (this) {
    is PrescriptionExtractionDto -> PrescriptionExtraction(
        medications = medications.map { item ->
            ExtractedMedication(
                name = item.name,
                strength = item.strength,
                dosage = item.dosage,
                frequency = item.frequency,
                times = item.times,
                duration = item.duration,
                mealRelation = item.mealRelation,
                confidence = item.confidence,
                selected = item.selected,
                warnings = item.warnings,
            )
        },
        overallConfidence = overallConfidence,
        warnings = warnings,
    )
    is LabReportExtractionDto -> LabReportExtraction(
        biomarkers = biomarkers.map { item ->
            ExtractedBiomarker(
                name = item.name,
                value = item.value,
                unit = item.unit,
                referenceRange = item.referenceRange,
                confidence = item.confidence,
                selected = item.selected,
                warnings = item.warnings,
            )
        },
        overallConfidence = overallConfidence,
        warnings = warnings,
    )
}

fun ExtractionResult.toDto(): ExtractionResultDto = when (this) {
    is PrescriptionExtraction -> PrescriptionExtractionDto(
        medications = medications.map { item ->
            ExtractedMedicationDto(
                name = item.name,
                strength = item.strength,
                dosage = item.dosage,
                frequency = item.frequency,
                times = item.times,
                duration = item.duration,
                mealRelation = item.mealRelation,
                confidence = item.confidence,
                selected = item.selected,
                warnings = item.warnings,
            )
        },
        overallConfidence = overallConfidence,
        warnings = warnings,
    )
    is LabReportExtraction -> LabReportExtractionDto(
        biomarkers = biomarkers.map { item ->
            ExtractedBiomarkerDto(
                name = item.name,
                value = item.value,
                unit = item.unit,
                referenceRange = item.referenceRange,
                confidence = item.confidence,
                selected = item.selected,
                warnings = item.warnings,
            )
        },
        overallConfidence = overallConfidence,
        warnings = warnings,
    )
}
