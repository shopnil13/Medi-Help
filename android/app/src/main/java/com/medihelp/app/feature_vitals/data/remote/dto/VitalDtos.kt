package com.medihelp.app.feature_vitals.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VitalCreateRequestDto(
    @SerialName("metric_type") val metricType: String,
    @SerialName("metric_name") val metricName: String? = null,
    @SerialName("value_numeric") val valueNumeric: Double,
    val unit: String,
    @SerialName("recorded_at") val recordedAt: String,
    val source: String,
    @SerialName("source_document_id") val sourceDocumentId: String? = null,
    val notes: String? = null,
)

@Serializable
data class VitalBulkCreateRequestDto(val records: List<VitalCreateRequestDto>)

@Serializable
data class VitalResponseDto(
    val id: String,
    @SerialName("metric_type") val metricType: String,
    @SerialName("metric_name") val metricName: String,
    @SerialName("value_numeric") val valueNumeric: Double,
    val unit: String,
    @SerialName("recorded_at") val recordedAt: String,
    val source: String,
    @SerialName("source_document_id") val sourceDocumentId: String? = null,
    @SerialName("source_job_id") val sourceJobId: String? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class ConfirmExtractedLabRequestDto(
    @SerialName("job_id") val jobId: String,
)

@Serializable
data class ConfirmExtractedLabResponseDto(
    val biomarkers: List<BiomarkerResponseDto>,
    @SerialName("vital_records") val vitalRecords: List<VitalResponseDto>,
)

@Serializable
data class BiomarkerResponseDto(
    val id: String,
    @SerialName("source_document_id") val sourceDocumentId: String,
    @SerialName("source_job_id") val sourceJobId: String? = null,
    val name: String,
    @SerialName("normalized_name") val normalizedName: String,
    @SerialName("value_numeric") val valueNumeric: Double? = null,
    @SerialName("value_text") val valueText: String? = null,
    val unit: String? = null,
    @SerialName("reference_range_text") val referenceRangeText: String? = null,
    val status: String,
    @SerialName("recorded_at") val recordedAt: String,
    @SerialName("confidence_score") val confidenceScore: Double? = null,
    @SerialName("created_at") val createdAt: String,
)
