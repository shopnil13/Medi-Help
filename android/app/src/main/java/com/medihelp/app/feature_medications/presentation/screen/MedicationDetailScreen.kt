package com.medihelp.app.feature_medications.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.core.designsystem.components.MediHelpConfirmationDialog
import com.medihelp.app.core.designsystem.components.MediHelpLoadingState
import com.medihelp.app.core.designsystem.components.MediHelpPrimaryButton
import com.medihelp.app.core.designsystem.components.MediHelpSecondaryButton
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.core.designsystem.theme.MediHelpShapes
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import com.medihelp.app.feature_medications.presentation.viewmodel.MedicationDetailViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MedicationDetailScreen(
    onBackClick: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: MedicationDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    Scaffold(
        topBar = { MediHelpTopBar(title = "Medicine details", onBackClick = onBackClick) },
    ) { padding ->
        val medication = uiState.medication
        if (medication == null) {
            MediHelpLoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        val needsDoctorGuidance = medication.requiresReview ||
            medication.purposeSimplified?.contains("ask your", ignoreCase = true) == true ||
            uiState.simplificationError != null

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(MediHelpSpacing.space6),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
        ) {
            item {
                Text(text = medication.name, style = MaterialTheme.typography.headlineMedium)
                medication.strength?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Text(text = "What it is for", style = MaterialTheme.typography.titleLarge)
                if (uiState.isSimplifying && medication.purposeSimplified == null) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                } else {
                    Text(
                        text = medication.purposeSimplified
                            ?: "Ask your doctor or pharmacist what this medicine is for.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            item {
                Text(text = "How to take it", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = medication.simplifiedInstruction ?: medication.dosageInstruction,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            item {
                TextButton(onClick = { showMore = !showMore }) {
                    Text("More details")
                    Icon(
                        if (showMore) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (showMore) "Hide details" else "Show details",
                    )
                }
                if (showMore) {
                    Column(verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2)) {
                        Text("Prescription instruction", style = MaterialTheme.typography.titleMedium)
                        Text(medication.dosageInstruction, style = MaterialTheme.typography.bodyMedium)
                        if (medication.schedules.isNotEmpty()) {
                            Text("Reminder times", style = MaterialTheme.typography.titleMedium)
                            val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                            medication.schedules.forEach { schedule ->
                                Text(
                                    text = schedule.timeOfDay.format(formatter) +
                                        (schedule.doseAmount?.let { " · $it" } ?: ""),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
            if (needsDoctorGuidance) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MediHelpShapes.small,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(MediHelpSpacing.space3),
                            horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.HealthAndSafety, contentDescription = null)
                            Text(
                                "Ask your doctor or pharmacist if this explanation is unclear.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            item {
                MediHelpSecondaryButton(
                    text = if (medication.status == MedicationStatus.ACTIVE) {
                        "Pause reminders"
                    } else {
                        "Resume reminders"
                    },
                    onClick = viewModel::togglePause,
                )
            }
            item {
                MediHelpPrimaryButton(
                    text = "Delete medicine",
                    onClick = { showDeleteConfirmation = true },
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        MediHelpConfirmationDialog(
            title = "Delete this medicine?",
            message = "Its reminders will be turned off. This can't be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.delete()
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }
}
