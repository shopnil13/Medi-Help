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
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
)

