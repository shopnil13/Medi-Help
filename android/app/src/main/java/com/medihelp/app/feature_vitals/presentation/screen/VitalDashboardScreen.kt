package com.medihelp.app.feature_vitals.presentation.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.core.designsystem.components.MediHelpEmptyState
import com.medihelp.app.core.designsystem.components.MediHelpLoadingState
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpShapes
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_vitals.domain.model.VitalMetricType
import com.medihelp.app.feature_vitals.domain.model.VitalRecord
import com.medihelp.app.feature_vitals.domain.model.VitalSource
import com.medihelp.app.feature_vitals.presentation.state.VitalMetricFilter
import com.medihelp.app.feature_vitals.presentation.state.VitalTimeRange
import com.medihelp.app.feature_vitals.presentation.viewmodel.VitalDashboardViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun VitalDashboardScreen(
    onAddVitalClick: () -> Unit,
    viewModel: VitalDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val visibleRecords = state.visibleRecords()
    val customNames = state.records
        .filter { it.metricType == VitalMetricType.CUSTOM }
        .map { it.metricName }
        .distinct()

    Scaffold(
        topBar = { MediHelpTopBar("Health chart") },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddVitalClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add vital")
            }
        },
    ) { padding ->
        if (state.isLoading) {
            MediHelpLoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                start = MediHelpSpacing.space4,
                top = MediHelpSpacing.space3,
                end = MediHelpSpacing.space4,
                bottom = MediHelpSpacing.space16,
            ),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
        ) {
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2),
                ) {
                    VitalMetricFilter.entries.forEach { metric ->
                        FilterChip(
                            selected = state.selectedMetric == metric,
                            onClick = { viewModel.selectMetric(metric) },
                            label = { Text(metric.label) },
                        )
                    }
                }
            }

            if (state.selectedMetric == VitalMetricFilter.CUSTOM && customNames.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2),
                    ) {
                        customNames.forEach { name ->
                            FilterChip(
                                selected = state.selectedCustomMetric == name,
                                onClick = { viewModel.selectCustomMetric(name) },
                                label = { Text(name) },
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2)) {
                    VitalTimeRange.entries.forEach { range ->
                        FilterChip(
                            selected = state.selectedRange == range,
                            onClick = { viewModel.selectRange(range) },
                            label = { Text(range.label) },
                        )
                    }
                }
            }

            if (visibleRecords.isEmpty()) {
                item {
                    MediHelpEmptyState(
                        message = "No ${state.selectedMetric.label.lowercase()} records in this range.",
                    )
                }
            } else {
                item { VitalLineChart(visibleRecords) }
                item {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = MediHelpSpacing.space2),
                    )
                }
                items(visibleRecords.asReversed(), key = { it.id }) { record ->
                    VitalPointCard(record)
                }
            }
        }
    }
}

@Composable
private fun VitalLineChart(records: List<VitalRecord>) {
    val producer = remember { CartesianChartModelProducer() }
    val systolic = records.filter { it.metricType == VitalMetricType.BLOOD_PRESSURE_SYSTOLIC }
    val diastolic = records.filter { it.metricType == VitalMetricType.BLOOD_PRESSURE_DIASTOLIC }

    LaunchedEffect(records) {
        producer.runTransaction {
            lineSeries {
                if (systolic.isNotEmpty() || diastolic.isNotEmpty()) {
                    if (systolic.isNotEmpty()) {
                        series(systolic.map { it.chartX }, systolic.map { it.value })
                    }
                    if (diastolic.isNotEmpty()) {
                        series(diastolic.map { it.chartX }, diastolic.map { it.value })
                    }
                } else {
                    series(records.map { it.chartX }, records.map { it.value })
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MediHelpShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
            modelProducer = producer,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(MediHelpSpacing.space3),
        )
    }
}

@Composable
private fun VitalPointCard(record: VitalRecord) {
    val zone = ZoneId.systemDefault()
    val dateTime = record.recordedAt.atZone(zone)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MediHelpShapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(MediHelpSpacing.space3),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space1),
            ) {
                Text(record.metricName, style = MaterialTheme.typography.titleMedium)
                Text(
                    dateTime.format(
                        DateTimeFormatter.ofLocalizedDateTime(
                            FormatStyle.MEDIUM,
                            FormatStyle.SHORT,
                        ),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space1)) {
                    Icon(
                        sourceIcon(record.source),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        record.source.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = "${formatValue(record.value)} ${record.unit}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = MediHelpSpacing.space2),
            )
        }
    }
}

private fun sourceIcon(source: VitalSource): ImageVector = when (source) {
    VitalSource.MANUAL -> Icons.Filled.Edit
    VitalSource.LAB_REPORT -> Icons.Filled.Description
    VitalSource.HEALTH_CONNECT, VitalSource.DEVICE -> Icons.Filled.Watch
    VitalSource.BACKEND_IMPORT -> Icons.Filled.HealthAndSafety
}

private fun formatValue(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.2f".format(value)

private val VitalRecord.chartX: Double
    get() = recordedAt.toEpochMilli() / 86_400_000.0
