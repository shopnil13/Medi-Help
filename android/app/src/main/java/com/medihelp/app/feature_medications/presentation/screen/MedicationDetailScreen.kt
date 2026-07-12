package com.medihelp.app.feature_medications.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.core.designsystem.components.MediHelpConfirmationDialog
import com.medihelp.app.core.designsystem.components.MediHelpLoadingState
import com.medihelp.app.core.designsystem.components.MediHelpPrimaryButton
import com.medihelp.app.core.designsystem.components.MediHelpSecondaryButton
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(MediHelpSpacing.space6),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
        ) {
            Text(text = medication.name, style = MaterialTheme.typography.headlineMedium)

            medication.strength?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(text = "How to take it", style = MaterialTheme.typography.titleLarge)
            Text(text = medication.dosageInstruction, style = MaterialTheme.typography.bodyLarge)

            if (medication.schedules.isNotEmpty()) {
                Text(text = "Reminder times", style = MaterialTheme.typography.titleLarge)
                val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                medication.schedules.forEach { schedule ->
                    Text(
                        text = schedule.timeOfDay.format(formatter) +
                            (schedule.doseAmount?.let { " · $it" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Text(
                text = "This may need medical attention. Please contact a doctor if you have questions about this medicine.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MediHelpSecondaryButton(
                text = if (medication.status == MedicationStatus.ACTIVE) "Pause reminders" else "Resume reminders",
                onClick = viewModel::togglePause,
            )

            MediHelpPrimaryButton(
                text = "Delete medicine",
                onClick = { showDeleteConfirmation = true },
            )
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
