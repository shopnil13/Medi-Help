package com.medihelp.app.feature_vitals.domain.model

import java.time.Instant

enum class VitalMetricType(
    val apiValue: String,
    val displayName: String,
    val defaultUnit: String,
) {
    HEART_RATE("heart_rate", "Heart rate", "bpm"),
    BLOOD_PRESSURE_SYSTOLIC("blood_pressure_systolic", "Systolic", "mmHg"),
    BLOOD_PRESSURE_DIASTOLIC("blood_pressure_diastolic", "Diastolic", "mmHg"),
    BLOOD_GLUCOSE("blood_glucose", "Blood glucose", "mg/dL"),
    WEIGHT("weight", "Weight", "kg"),
    CUSTOM("custom", "Custom", ""),
    ;

    companion object {
        fun fromApi(value: String): VitalMetricType = entries.firstOrNull { it.apiValue == value } ?: CUSTOM
    }
}

enum class VitalSource(val apiValue: String, val displayName: String) {
    MANUAL("manual", "Manual"),
    LAB_REPORT("lab_report", "Lab Report"),
    HEALTH_CONNECT("health_connect", "Health Connect"),
    DEVICE("device", "Device"),
    BACKEND_IMPORT("backend_import", "Imported"),
    ;

    companion object {
        fun fromApi(value: String): VitalSource = entries.firstOrNull { it.apiValue == value } ?: BACKEND_IMPORT
    }
}

data class VitalRecord(
    val id: String,
    val metricType: VitalMetricType,
    val metricName: String,
    val value: Double,
    val unit: String,
    val recordedAt: Instant,
    val source: VitalSource,
    val sourceDocumentId: String?,
    val sourceJobId: String? = null,
    val notes: String?,
    val isSynced: Boolean,
)

data class NewVitalInput(
    val metricType: VitalMetricType,
    val metricName: String? = null,
    val value: Double,
    val unit: String,
    val recordedAt: Instant,
    val source: VitalSource = VitalSource.MANUAL,
    val sourceDocumentId: String? = null,
    val notes: String? = null,
)
