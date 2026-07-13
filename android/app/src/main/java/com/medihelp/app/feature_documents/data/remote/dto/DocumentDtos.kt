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
    val document: DocumentResponseDto,
)

@Serializable
data class DocumentUploadResponseDto(
    val document: DocumentResponseDto,
    val job: ProcessingJobResponseDto,
)
