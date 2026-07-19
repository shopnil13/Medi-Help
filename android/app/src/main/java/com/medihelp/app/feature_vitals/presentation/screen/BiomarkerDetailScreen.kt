package com.medihelp.app.feature_vitals.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.core.designsystem.components.MediHelpErrorState
import com.medihelp.app.core.designsystem.components.MediHelpLoadingState
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpShapes
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_vitals.presentation.viewmodel.BiomarkerDetailViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun BiomarkerDetailScreen(
    onBackClick: () -> Unit,
    viewModel: BiomarkerDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showMore by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MediHelpTopBar(title = "Lab explanation", onBackClick = onBackClick) },
    ) { padding ->
        when {
            state.isLoading -> MediHelpLoadingState(modifier = Modifier.padding(padding))
            state.errorMessage != null -> MediHelpErrorState(
                message = state.errorMessage.orEmpty(),
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            state.biomarker != null -> {
                val biomarker = checkNotNull(state.biomarker)
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(MediHelpSpacing.space6),
                    verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
                ) {
                    item {
                        Text(biomarker.name, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = listOfNotNull(biomarker.value, biomarker.unit).joinToString(" "),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = biomarker.recordedAt.atZone(ZoneId.systemDefault()).format(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item {
                        Text("What it measures", style = MaterialTheme.typography.titleLarge)
                        Text(biomarker.explanation, style = MaterialTheme.typography.bodyLarge)
                    }
                    item {
                        Text("What this comparison means", style = MaterialTheme.typography.titleLarge)
                        Text(biomarker.statusExplanation, style = MaterialTheme.typography.bodyLarge)
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
                                Text(biomarker.moreDetails, style = MaterialTheme.typography.bodyMedium)
                                biomarker.referenceRange?.let {
                                    Text(
                                        "Lab reference range: $it",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                    if (biomarker.askDoctor) {
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
                                        "Ask your doctor what this result means for you.",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
