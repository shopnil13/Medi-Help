package com.medihelp.app.feature_vitals.domain.model

import java.time.Instant

/**
 * A single value ready for display, kept with the timestamp it was recorded at
 * so the UI can always show provenance next to the number.
 */
data class VitalReading(
    val displayValue: String,
    val unit: String,
    val recordedAt: Instant,
)

/** Latest reading for each metric shown on the dashboard summary card. */
data class VitalsSummary(
    val bloodPressure: VitalReading? = null,
    val heartRate: VitalReading? = null,
    val glucose: VitalReading? = null,
) {
    val hasAnyReading: Boolean
        get() = bloodPressure != null || heartRate != null || glucose != null
}

/**
 * Reduces a full vitals history to the most recent reading per dashboard metric.
 *
 * Blood pressure is only reported when a systolic and a diastolic value share
 * the same timestamp. Pairing the latest systolic with an unrelated diastolic
 * would invent a reading the user never took, so an unpaired value is dropped
 * rather than shown alone.
 */
fun List<VitalRecord>.toDashboardSummary(): VitalsSummary {
    val latestByType = groupBy { it.metricType }
        .mapValues { (_, records) -> records.maxByOrNull { it.recordedAt } }

    val systolic = latestByType[VitalMetricType.BLOOD_PRESSURE_SYSTOLIC]
    val diastolic = this
        .filter { it.metricType == VitalMetricType.BLOOD_PRESSURE_DIASTOLIC }
        .firstOrNull { it.recordedAt == systolic?.recordedAt }

    val bloodPressure = if (systolic != null && diastolic != null) {
        VitalReading(
            displayValue = "${systolic.value.formatMeasurement()}/${diastolic.value.formatMeasurement()}",
            unit = systolic.unit,
            recordedAt = systolic.recordedAt,
        )
    } else {
        null
    }

    return VitalsSummary(
        bloodPressure = bloodPressure,
        heartRate = latestByType[VitalMetricType.HEART_RATE]?.toReading(),
        glucose = latestByType[VitalMetricType.BLOOD_GLUCOSE]?.toReading(),
    )
}

private fun VitalRecord.toReading() = VitalReading(
    displayValue = value.formatMeasurement(),
    unit = unit,
    recordedAt = recordedAt,
)

/** Drops the decimal part for whole numbers so "72.0 bpm" reads as "72 bpm". */
internal fun Double.formatMeasurement(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()
