package com.medihelp.app.feature_medications.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.core.designsystem.components.MediHelpPrimaryButton
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_medications.presentation.state.MEAL_RELATION_OPTIONS
import com.medihelp.app.feature_medications.presentation.viewmodel.AddMedicationViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSucceeded) {
        if (uiState.saveSucceeded) onSaveSuccess()
    }

    Scaffold(
        topBar = { MediHelpTopBar(title = "Add a medicine", onBackClick = onBackClick) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(MediHelpSpacing.space6),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Medicine name") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MediHelpSpacing.tapTargetMin + 8.dp),
            )

            OutlinedTextField(
                value = uiState.strength,
                onValueChange = viewModel::onStrengthChange,
                label = { Text("Strength (optional, e.g. 500mg)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MediHelpSpacing.tapTargetMin + 8.dp),
            )

            OutlinedTextField(
                value = uiState.dosageInstruction,
                onValueChange = viewModel::onDosageInstructionChange,
                label = { Text("How to take it (e.g. 1 tablet after breakfast)") },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            OutlinedTextField(
                value = uiState.doseAmount,
                onValueChange = viewModel::onDoseAmountChange,
                label = { Text("Dose amount (optional, e.g. 1 tablet)") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MediHelpSpacing.tapTargetMin + 8.dp),
            )

            Text(text = "Reminder time", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MediHelpSpacing.tapTargetMin),
            ) {
                Text(
                    text = uiState.timeOfDay.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Text(text = "When to take it", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2),
            ) {
                MEAL_RELATION_OPTIONS.forEach { (value, label) ->
                    FilterChip(
                        selected = uiState.mealRelation == value,
                        onClick = { viewModel.onMealRelationChange(value) },
                        label = { Text(label) },
                    )
                }
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(MediHelpSpacing.space2))

            MediHelpPrimaryButton(
                text = "Add to my medicines",
                onClick = viewModel::save,
                isLoading = uiState.isLoading,
            )
        }
    }

    if (showTimePicker) {
        MedicineTimePickerDialog(
            initialTime = uiState.timeOfDay,
            onConfirm = { time ->
                viewModel.onTimeChange(time)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineTimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text("Set time")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = { TimePicker(state = state) },
    )
}
