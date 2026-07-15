package com.medihelp.app.feature_vitals.presentation.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.core.designsystem.components.MediHelpPrimaryButton
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_vitals.presentation.state.AddVitalKind
import com.medihelp.app.feature_vitals.presentation.viewmodel.AddVitalViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVitalScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AddVitalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()
    val localDateTime = state.recordedAt.atZone(zone)

    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) onSaveSuccess()
    }

    Scaffold(
        topBar = { MediHelpTopBar(title = "Add a vital", onBackClick = onBackClick) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(MediHelpSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2),
            ) {
                AddVitalKind.entries.forEach { kind ->
                    FilterChip(
                        selected = state.kind == kind,
                        onClick = { viewModel.selectKind(kind) },
                        label = { Text(kind.label) },
                    )
                }
            }

            if (state.kind == AddVitalKind.CUSTOM) {
                OutlinedTextField(
                    value = state.customName,
                    onValueChange = viewModel::onCustomNameChange,
                    label = { Text("Marker name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (state.kind == AddVitalKind.BLOOD_PRESSURE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
                ) {
                    VitalNumberField(
                        value = state.primaryValue,
                        onValueChange = viewModel::onPrimaryValueChange,
                        label = "Systolic",
                        modifier = Modifier.weight(1f),
                    )
                    VitalNumberField(
                        value = state.secondaryValue,
                        onValueChange = viewModel::onSecondaryValueChange,
                        label = "Diastolic",
                        modifier = Modifier.weight(1f),
                    )
                }
                Text("mmHg")
            } else {
                VitalNumberField(
                    value = state.primaryValue,
                    onValueChange = viewModel::onPrimaryValueChange,
                    label = "Value",
                    modifier = Modifier.fillMaxWidth(),
                )
                UnitControl(
                    kind = state.kind,
                    unit = state.unit,
                    onUnitChange = viewModel::onUnitChange,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                    Text(
                        localDateTime.toLocalDate().format(
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM),
                        ),
                    )
                }
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null)
                    Text(
                        localDateTime.toLocalTime().format(
                            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT),
                        ),
                    )
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            state.errorMessage?.let { error ->
                Text(error, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }

            MediHelpPrimaryButton(
                text = "Save vital",
                onClick = viewModel::save,
                isLoading = state.isSaving,
            )
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = localDateTime.toLocalDate()
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { selected ->
                            val date = Instant.ofEpochMilli(selected)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.onRecordedAtChange(
                                date.atTime(localDateTime.toLocalTime()).atZone(zone).toInstant(),
                            )
                        }
                        showDatePicker = false
                    },
                ) { Text("Set date") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) { DatePicker(state = pickerState) }
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = localDateTime.hour,
            initialMinute = localDateTime.minute,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = localDateTime.toLocalDate()
                            .atTime(pickerState.hour, pickerState.minute)
                            .atZone(zone)
                            .toInstant()
                        viewModel.onRecordedAtChange(updated)
                        showTimePicker = false
                    },
                ) { Text("Set time") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = pickerState) },
        )
    }
}

@Composable
private fun VitalNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier,
    )
}

@Composable
private fun UnitControl(
    kind: AddVitalKind,
    unit: String,
    onUnitChange: (String) -> Unit,
) {
    val options = when (kind) {
        AddVitalKind.BLOOD_GLUCOSE -> listOf("mg/dL", "mmol/L")
        AddVitalKind.WEIGHT -> listOf("kg", "lb")
        AddVitalKind.HEART_RATE -> listOf("bpm")
        else -> emptyList()
    }
    if (options.isEmpty()) {
        OutlinedTextField(
            value = unit,
            onValueChange = onUnitChange,
            label = { Text("Unit") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2)) {
            options.forEach { option ->
                FilterChip(
                    selected = unit == option,
                    onClick = { onUnitChange(option) },
                    label = { Text(option) },
                )
            }
        }
    }
}
