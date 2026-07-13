package com.medihelp.app.feature_documents.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.core.designsystem.components.MediHelpErrorState
import com.medihelp.app.core.designsystem.components.MediHelpLoadingState
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_documents.presentation.viewmodel.ProcessingStatusViewModel

@Composable
fun ProcessingStatusScreen(
    onBackClick: () -> Unit,
    viewModel: ProcessingStatusViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { MediHelpTopBar(title = "Document status", onBackClick = onBackClick) },
    ) { padding ->
        when {
            uiState.isLoading -> MediHelpLoadingState(Modifier.padding(padding))
            uiState.document == null -> MediHelpErrorState(
                message = uiState.refreshError ?: "Document status is unavailable.",
                modifier = Modifier.padding(padding),
                onRetry = viewModel::refresh,
            )
            else -> {
                val document = checkNotNull(uiState.document)
                val isFailed = document.jobStatus == "failed"
                val isComplete = document.jobStatus in setOf("completed", "needs_review")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(MediHelpSpacing.space6),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
                ) {
                    Icon(
                        imageVector = when {
                            isFailed -> Icons.Filled.Error
                            isComplete -> Icons.Filled.CheckCircle
                            else -> Icons.Filled.HourglassTop
                        },
                        contentDescription = null,
                        tint = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                    Text(document.originalFilename, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = document.jobStatus.replace('_', ' ').replaceFirstChar(Char::uppercase),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    LinearProgressIndicator(
                        progress = { document.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    document.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    uiState.refreshError?.let {
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
