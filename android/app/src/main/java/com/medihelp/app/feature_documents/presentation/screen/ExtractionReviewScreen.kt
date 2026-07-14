package com.medihelp.app.feature_documents.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.core.designsystem.components.MediHelpErrorState
import com.medihelp.app.core.designsystem.components.MediHelpLoadingState
import com.medihelp.app.core.designsystem.components.MediHelpPrimaryButton
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_documents.domain.model.ExtractedBiomarker
import com.medihelp.app.feature_documents.domain.model.ExtractedMedication
import com.medihelp.app.feature_documents.domain.model.LabReportExtraction
import com.medihelp.app.feature_documents.domain.model.PrescriptionExtraction
import com.medihelp.app.feature_documents.presentation.state.ExtractionReviewUiState
import com.medihelp.app.feature_documents.presentation.viewmodel.ExtractionReviewViewModel
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import kotlin.math.roundToInt

@Composable
fun ExtractionReviewRoute(
    onBackClick: () -> Unit,
    onLabConfirmed: () -> Unit,
    onPrescriptionDone: () -> Unit,
    viewModel: ExtractionReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isConfirmed) {
        if (state.isConfirmed && state.draft is LabReportExtraction) onLabConfirmed()
    }

    if (state.isConfirmed && state.draft is PrescriptionExtraction) {
        PrescriptionImportSuccessScreen(
            state = state,
            onReminderEnabled = viewModel::setReminderEnabled,
            onDone = onPrescriptionDone,
        )
        return
    }

    when (state.draft) {
        is PrescriptionExtraction -> ExtractedPrescriptionReviewScreen(
            state = state,
            onBackClick = onBackClick,
            onMedicationChange = viewModel::updateMedication,
            onConfirm = viewModel::confirm,
        )
        is LabReportExtraction -> ExtractedLabReportReviewScreen(
            state = state,
            onBackClick = onBackClick,
            onBiomarkerChange = viewModel::updateBiomarker,
            onConfirm = viewModel::confirm,
        )
        null -> if (state.isLoading) {
            MediHelpLoadingState()
        } else {
            MediHelpErrorState(state.errorMessage ?: "No extracted data is available.")
        }
    }
}

@Composable
private fun PrescriptionImportSuccessScreen(
    state: ExtractionReviewUiState,
    onReminderEnabled: (String, Boolean) -> Unit,
    onDone: () -> Unit,
) {
    val reminderCount = state.importedMedications
        .filter { it.status == MedicationStatus.ACTIVE }
        .sumOf { it.schedules.size }
    Scaffold(topBar = { MediHelpTopBar("Medicines added") }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(MediHelpSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "${state.importedMedications.size} medicines added",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        "$reminderCount reminders scheduled",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            itemsIndexed(state.importedMedications, key = { _, item -> item.id }) { _, medication ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(MediHelpSpacing.space4),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(medication.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${medication.schedules.size} reminders",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = medication.status == MedicationStatus.ACTIVE,
                            onCheckedChange = { enabled -> onReminderEnabled(medication.id, enabled) },
                        )
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2)) {
                    state.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    MediHelpPrimaryButton(text = "Done", onClick = onDone)
                }
            }
        }
    }
}

@Composable
fun ExtractedPrescriptionReviewScreen(
    state: ExtractionReviewUiState,
    onBackClick: () -> Unit,
    onMedicationChange: (Int, (ExtractedMedication) -> ExtractedMedication) -> Unit,
    onConfirm: () -> Unit,
) {
    val extraction = state.draft as PrescriptionExtraction
    Scaffold(topBar = { MediHelpTopBar("Review prescription", onBackClick = onBackClick) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(MediHelpSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
        ) {
            item {
                ReviewSummary(extraction.overallConfidence, extraction.warnings)
            }
            itemsIndexed(extraction.medications) { index, medication ->
                MedicationReviewCard(
                    medication = medication,
                    onChange = { transform -> onMedicationChange(index, transform) },
                )
            }
            item {
                ReviewFooter(
                    state = state,
                    hasSelection = extraction.medications.any { it.selected },
                    onConfirm = onConfirm,
                )
            }
        }
    }
}

@Composable
fun ExtractedLabReportReviewScreen(
    state: ExtractionReviewUiState,
    onBackClick: () -> Unit,
    onBiomarkerChange: (Int, (ExtractedBiomarker) -> ExtractedBiomarker) -> Unit,
    onConfirm: () -> Unit,
) {
    val extraction = state.draft as LabReportExtraction
    Scaffold(topBar = { MediHelpTopBar("Review lab report", onBackClick = onBackClick) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(MediHelpSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
        ) {
            item {
                ReviewSummary(extraction.overallConfidence, extraction.warnings)
            }
            itemsIndexed(extraction.biomarkers) { index, biomarker ->
                BiomarkerReviewCard(
                    biomarker = biomarker,
                    onChange = { transform -> onBiomarkerChange(index, transform) },
                )
            }
            item {
                ReviewFooter(
                    state = state,
                    hasSelection = extraction.biomarkers.any { it.selected },
                    onConfirm = onConfirm,
                )
            }
        }
    }
}

@Composable
private fun MedicationReviewCard(
    medication: ExtractedMedication,
    onChange: ((ExtractedMedication) -> ExtractedMedication) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(MediHelpSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
        ) {
            SelectionHeader(
                selected = medication.selected,
                confidence = medication.confidence,
                onSelectedChange = { selected -> onChange { it.copy(selected = selected) } },
            )
            OutlinedTextField(
                value = medication.name,
                onValueChange = { value -> onChange { it.copy(name = value) } },
                label = { Text("Medicine") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = medication.strength.orEmpty(),
                onValueChange = { value -> onChange { it.copy(strength = value.ifBlank { null }) } },
                label = { Text("Strength") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = medication.dosage.orEmpty(),
                onValueChange = { value -> onChange { it.copy(dosage = value.ifBlank { null }) } },
                label = { Text("Dosage") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = medication.frequency.orEmpty(),
                onValueChange = { value -> onChange { it.copy(frequency = value.ifBlank { null }) } },
                label = { Text("Frequency") },
                modifier = Modifier.fillMaxWidth(),
            )
            WarningList(medication.warnings)
        }
    }
}

@Composable
private fun BiomarkerReviewCard(
    biomarker: ExtractedBiomarker,
    onChange: ((ExtractedBiomarker) -> ExtractedBiomarker) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(MediHelpSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
        ) {
            SelectionHeader(
                selected = biomarker.selected,
                confidence = biomarker.confidence,
                onSelectedChange = { selected -> onChange { it.copy(selected = selected) } },
            )
            OutlinedTextField(
                value = biomarker.name,
                onValueChange = { value -> onChange { it.copy(name = value) } },
                label = { Text("Biomarker") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = biomarker.value,
                onValueChange = { value -> onChange { it.copy(value = value) } },
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = biomarker.unit.orEmpty(),
                onValueChange = { value -> onChange { it.copy(unit = value.ifBlank { null }) } },
                label = { Text("Unit") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = biomarker.referenceRange.orEmpty(),
                onValueChange = { value -> onChange { it.copy(referenceRange = value.ifBlank { null }) } },
                label = { Text("Reference range") },
                modifier = Modifier.fillMaxWidth(),
            )
            WarningList(biomarker.warnings)
        }
    }
}

@Composable
private fun SelectionHeader(
    selected: Boolean,
    confidence: Double,
    onSelectedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = selected, onCheckedChange = onSelectedChange)
            Text("Include", style = MaterialTheme.typography.labelLarge)
        }
        Text(
            text = "${(confidence * 100).roundToInt()}% confidence",
            style = MaterialTheme.typography.labelMedium,
            color = if (confidence < 0.75) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
    }
}

@Composable
private fun ReviewSummary(confidence: Double, warnings: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2)) {
        Text(
            text = "Overall confidence: ${(confidence * 100).roundToInt()}%",
            style = MaterialTheme.typography.titleMedium,
        )
        WarningList(warnings)
    }
}

@Composable
private fun WarningList(warnings: List<String>) {
    warnings.distinct().forEach { warning ->
        Row(horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2)) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = warning,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ReviewFooter(
    state: ExtractionReviewUiState,
    hasSelection: Boolean,
    onConfirm: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2)) {
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        MediHelpPrimaryButton(
            text = "Confirm selected",
            onClick = onConfirm,
            enabled = hasSelection,
            isLoading = state.isSaving,
        )
    }
}
