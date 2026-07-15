package com.medihelp.app.feature_vitals.presentation.state

import com.medihelp.app.feature_vitals.domain.model.VitalMetricType
import com.medihelp.app.feature_vitals.domain.model.VitalRecord
import com.medihelp.app.feature_vitals.domain.model.VitalSource
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class VitalDashboardUiStateTest {
    @Test
    fun filtersByMetricCustomNameAndDateRange() {
        val now = Instant.parse("2026-07-13T12:00:00Z")
        val records = listOf(
            record("recent", VitalMetricType.CUSTOM, "Temperature", "2026-07-12T12:00:00Z"),
            record("other", VitalMetricType.CUSTOM, "Oxygen", "2026-07-12T12:00:00Z"),
            record("old", VitalMetricType.CUSTOM, "Temperature", "2026-06-01T12:00:00Z"),
            record("heart", VitalMetricType.HEART_RATE, "Heart rate", "2026-07-12T12:00:00Z"),
        )
        val state = VitalDashboardUiState(
            records = records,
            selectedMetric = VitalMetricFilter.CUSTOM,
            selectedCustomMetric = "Temperature",
            selectedRange = VitalTimeRange.SEVEN_DAYS,
        )

        assertEquals(listOf("recent"), state.visibleRecords(now).map { it.id })
    }

    private fun record(
        id: String,
        type: VitalMetricType,
        name: String,
        recordedAt: String,
    ) = VitalRecord(
        id = id,
        metricType = type,
        metricName = name,
        value = 1.0,
        unit = "unit",
        recordedAt = Instant.parse(recordedAt),
        source = VitalSource.MANUAL,
        sourceDocumentId = null,
        notes = null,
        isSynced = true,
    )
}

