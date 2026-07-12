package com.medihelp.app.feature_medications.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.core.designsystem.components.MediHelpEmptyState
import com.medihelp.app.core.designsystem.components.MediHelpLoadingState
import com.medihelp.app.core.designsystem.theme.MediHelpShapes
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_medications.domain.model.Medication
import com.medihelp.app.feature_medications.presentation.viewmodel.MedicationListViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MedicationListScreen(
    onAddMedicationClick: () -> Unit,
    onMedicationClick: (String) -> Unit,
    viewModel: MedicationListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedicationClick) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add medicine")
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> MediHelpLoadingState(modifier = Modifier.padding(padding))
            uiState.medications.isEmpty() -> MediHelpEmptyState(
                message = "You haven't added any medicines yet. Tap + to add one.",
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(MediHelpSpacing.space4),
                verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
            ) {
                items(uiState.medications, key = { it.id }) { medication ->
                    MedicationCard(medication = medication, onClick = { onMedicationClick(medication.id) })
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(medication: Medication, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MediHelpShapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(MediHelpSpacing.space4)) {
            Text(text = medication.name, style = MaterialTheme.typography.titleLarge)

            medication.strength?.let { strength ->
                Text(
                    text = strength,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = medication.dosageInstruction,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            val times = medication.schedules.joinToString(", ") { it.timeOfDay.format(timeFormatter) }
            if (times.isNotBlank()) {
                Text(
                    text = times,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
