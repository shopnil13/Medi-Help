package com.medihelp.app.feature_vitals.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VitalsSummaryTest {

    private val morning: Instant = Instant.parse("2026-07-19T08:00:00Z")
    private val evening: Instant = Instant.parse("2026-07-19T20:00:00Z")

    @Test
    fun `empty history produces an empty summary`() {
        val summary = emptyList<VitalRecord>().toDashboardSummary()

        assertFalse(summary.hasAnyReading)
        assertNull(summary.bloodPressure)
    }

    @Test
    fun `systolic and diastolic recorded together pair into one reading`() {
        val summary = listOf(
            record(VitalMetricType.BLOOD_PRESSURE_SYSTOLIC, 120.0, "mmHg", morning),
            record(VitalMetricType.BLOOD_PRESSURE_DIASTOLIC, 80.0, "mmHg", morning),
        ).toDashboardSummary()

        assertEquals("120/80", summary.bloodPressure?.displayValue)
        assertEquals("mmHg", summary.bloodPressure?.unit)
    }

    @Test
    fun `unpaired systolic is dropped rather than shown alone`() {
        val summary = listOf(
            record(VitalMetricType.BLOOD_PRESSURE_SYSTOLIC, 120.0, "mmHg", morning),
        ).toDashboardSummary()

        assertNull(summary.bloodPressure)
    }

    @Test
    fun `systolic is never paired with a diastolic from another reading`() {
        val summary = listOf(
            record(VitalMetricType.BLOOD_PRESSURE_SYSTOLIC, 135.0, "mmHg", evening),
            record(VitalMetricType.BLOOD_PRESSURE_DIASTOLIC, 80.0, "mmHg", morning),
        ).toDashboardSummary()

        assertNull(summary.bloodPressure)
    }

    @Test
    fun `latest blood pressure pair wins over an older one`() {
        val summary = listOf(
            record(VitalMetricType.BLOOD_PRESSURE_SYSTOLIC, 120.0, "mmHg", morning),
            record(VitalMetricType.BLOOD_PRESSURE_DIASTOLIC, 80.0, "mmHg", morning),
            record(VitalMetricType.BLOOD_PRESSURE_SYSTOLIC, 130.0, "mmHg", evening),
            record(VitalMetricType.BLOOD_PRESSURE_DIASTOLIC, 85.0, "mmHg", evening),
        ).toDashboardSummary()

        assertEquals("130/85", summary.bloodPressure?.displayValue)
    }

    @Test
    fun `heart rate and glucose report their most recent value`() {
        val summary = listOf(
            record(VitalMetricType.HEART_RATE, 68.0, "bpm", morning),
            record(VitalMetricType.HEART_RATE, 72.0, "bpm", evening),
            record(VitalMetricType.BLOOD_GLUCOSE, 110.0, "mg/dL", morning),
        ).toDashboardSummary()

        assertEquals("72", summary.heartRate?.displayValue)
        assertEquals("110", summary.glucose?.displayValue)
        assertTrue(summary.hasAnyReading)
    }

    @Test
    fun `decimal measurements keep their fraction`() {
        val summary = listOf(
            record(VitalMetricType.BLOOD_GLUCOSE, 5.4, "mmol/L", morning),
        ).toDashboardSummary()

        assertEquals("5.4", summary.glucose?.displayValue)
    }

    private fun record(
        metricType: VitalMetricType,
        value: Double,
        unit: String,
        recordedAt: Instant,
    ) = VitalRecord(
        id = "$metricType-$recordedAt",
        metricType = metricType,
        metricName = metricType.displayName,
        value = value,
        unit = unit,
        recordedAt = recordedAt,
        source = VitalSource.MANUAL,
        sourceDocumentId = null,
        notes = null,
        isSynced = true,
    )
}
