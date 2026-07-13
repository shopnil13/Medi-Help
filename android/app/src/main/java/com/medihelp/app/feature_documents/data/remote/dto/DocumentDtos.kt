package com.medihelp.app.feature_documents.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentResponseDto(
    val id: String,
    @SerialName("document_type") val documentType: String,
    @SerialName("original_filename") val originalFilename: String,
    @SerialName("content_type") val contentType: String,
    @SerialName("file_size_bytes") val fileSizeBytes: Long,
    val status: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class ProcessingJobResponseDto(
    val id: String,
    @SerialName("document_id") val documentId: String,
    val status: String,
    @SerialName("progress_percent") val progressPercent: Int,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("structured_result") val structuredResult: ExtractionResultDto? = null,
    @SerialName("confirmed_result") val confirmedResult: ExtractionResultDto? = null,
    val document: DocumentResponseDto,
)

@Serializable
data class DocumentUploadResponseDto(
    val document: DocumentResponseDto,
    val job: ProcessingJobResponseDto,
)

@Serializable
sealed interface ExtractionResultDto

@Serializable
@SerialName("prescription")
data class PrescriptionExtractionDto(
    val medications: List<ExtractedMedicationDto>,
    @SerialName("overall_confidence") val overallConfidence: Double,
    @SerialName("requires_confirmation") val requiresConfirmation: Boolean = true,
    val warnings: List<String> = emptyList(),
) : ExtractionResultDto

@Serializable
data class ExtractedMedicationDto(
    val name: String,
    val strength: String? = null,
    val dosage: String? = null,
    val frequency: String? = null,
    val times: List<String> = emptyList(),
    val duration: String? = null,
    @SerialName("meal_relation") val mealRelation: String? = null,
    val confidence: Double,
    val selected: Boolean = true,
    val warnings: List<String> = emptyList(),
)

@Serializable
@SerialName("lab_report")
data class LabReportExtractionDto(
    val biomarkers: List<ExtractedBiomarkerDto>,
    @SerialName("overall_confidence") val overallConfidence: Double,
    @SerialName("requires_confirmation") val requiresConfirmation: Boolean = true,
    val warnings: List<String> = emptyList(),
) : ExtractionResultDto

@Serializable
data class ExtractedBiomarkerDto(
    val name: String,
    val value: String,
    val unit: String? = null,
    @SerialName("reference_range") val referenceRange: String? = null,
    val confidence: Double,
    val selected: Boolean = true,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class ConfirmExtractionRequestDto(val result: ExtractionResultDto)
