package com.medihelp.app.feature_vitals.data.mapper

import com.medihelp.app.feature_vitals.data.local.entity.VitalRecordEntity
import com.medihelp.app.feature_vitals.data.remote.dto.VitalCreateRequestDto
import com.medihelp.app.feature_vitals.data.remote.dto.VitalResponseDto
import com.medihelp.app.feature_vitals.domain.model.NewVitalInput
import com.medihelp.app.feature_vitals.domain.model.VitalMetricType
import com.medihelp.app.feature_vitals.domain.model.VitalRecord
import com.medihelp.app.feature_vitals.domain.model.VitalSource
import java.time.Instant
import java.util.UUID

fun VitalRecordEntity.toDomain() = VitalRecord(
    id = id,
    metricType = VitalMetricType.fromApi(metricType),
    metricName = metricName,
    value = valueNumeric,
    unit = unit,
    recordedAt = Instant.ofEpochMilli(recordedAtEpochMillis),
    source = VitalSource.fromApi(source),
    sourceDocumentId = sourceDocumentId,
    sourceJobId = sourceJobId,
    notes = notes,
    isSynced = isSynced,
)

fun NewVitalInput.toLocalEntity(now: Long = System.currentTimeMillis()) = VitalRecordEntity(
    id = "local-${UUID.randomUUID()}",
    serverId = null,
    metricType = metricType.apiValue,
    metricName = metricName?.trim().orEmpty().ifBlank { metricType.displayName },
    valueNumeric = value,
    unit = unit.trim(),
    recordedAtEpochMillis = recordedAt.toEpochMilli(),
    source = source.apiValue,
    sourceDocumentId = sourceDocumentId,
    sourceJobId = null,
    notes = notes?.trim()?.ifBlank { null },
    isSynced = false,
    createdAtEpochMillis = now,
)

fun VitalRecordEntity.toCreateRequest() = VitalCreateRequestDto(
    metricType = metricType,
    metricName = metricName.takeIf { metricType == VitalMetricType.CUSTOM.apiValue },
    valueNumeric = valueNumeric,
    unit = unit,
    recordedAt = Instant.ofEpochMilli(recordedAtEpochMillis).toString(),
    source = source,
    sourceDocumentId = sourceDocumentId,
    notes = notes,
)

fun VitalResponseDto.toEntity() = VitalRecordEntity(
    id = id,
    serverId = id,
    metricType = metricType,
    metricName = metricName,
    valueNumeric = valueNumeric,
    unit = unit,
    recordedAtEpochMillis = Instant.parse(recordedAt).toEpochMilli(),
    source = source,
    sourceDocumentId = sourceDocumentId,
    sourceJobId = sourceJobId,
    notes = notes,
    isSynced = true,
    createdAtEpochMillis = Instant.parse(createdAt).toEpochMilli(),
)
