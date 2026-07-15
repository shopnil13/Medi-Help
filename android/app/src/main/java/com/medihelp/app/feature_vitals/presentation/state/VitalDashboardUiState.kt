package com.medihelp.app.feature_vitals.presentation.state

import com.medihelp.app.feature_vitals.domain.model.VitalMetricType
import com.medihelp.app.feature_vitals.domain.model.VitalRecord
import java.time.Instant
import java.time.temporal.ChronoUnit

enum class VitalMetricFilter(val label: String) {
    HEART_RATE("Heart rate"),
    BLOOD_PRESSURE("Blood pressure"),
    BLOOD_GLUCOSE("Glucose"),
    WEIGHT("Weight"),
    CUSTOM("Custom"),
}

enum class VitalTimeRange(val label: String, val days: Long?) {
    SEVEN_DAYS("7 days", 7),
    THIRTY_DAYS("30 days", 30),
    ALL("All", null),
}

data class VitalDashboardUiState(
    val records: List<VitalRecord> = emptyList(),
    val selectedMetric: VitalMetricFilter = VitalMetricFilter.HEART_RATE,
    val selectedCustomMetric: String? = null,
    val selectedRange: VitalTimeRange = VitalTimeRange.THIRTY_DAYS,
    val isLoading: Boolean = true,
) {
    fun visibleRecords(now: Instant = Instant.now()): List<VitalRecord> {
        val cutoff = selectedRange.days?.let { now.minus(it, ChronoUnit.DAYS) }
        return records
            .filter { record -> selectedMetric.includes(record.metricType) }
            .filter { record ->
                selectedMetric != VitalMetricFilter.CUSTOM ||
                    selectedCustomMetric == null ||
                    record.metricName == selectedCustomMetric
            }
            .filter { record -> cutoff == null || !record.recordedAt.isBefore(cutoff) }
            .sortedBy { it.recordedAt }
    }
}

private fun VitalMetricFilter.includes(type: VitalMetricType): Boolean = when (this) {
    VitalMetricFilter.HEART_RATE -> type == VitalMetricType.HEART_RATE
    VitalMetricFilter.BLOOD_PRESSURE -> type == VitalMetricType.BLOOD_PRESSURE_SYSTOLIC ||
        type == VitalMetricType.BLOOD_PRESSURE_DIASTOLIC
    VitalMetricFilter.BLOOD_GLUCOSE -> type == VitalMetricType.BLOOD_GLUCOSE
    VitalMetricFilter.WEIGHT -> type == VitalMetricType.WEIGHT
    VitalMetricFilter.CUSTOM -> type == VitalMetricType.CUSTOM
}
