package com.medihelp.app.feature_dashboard.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.medihelp.app.core.designsystem.components.MediHelpEmptyState
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_dashboard.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLoggedOut: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.hasLoggedOut) {
        if (uiState.hasLoggedOut) {
            onLoggedOut()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good morning,",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = uiState.displayName ?: "there",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutConfirmation = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log out",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        MediHelpEmptyState(
            message = "Your medicines, vitals, and health tips will show up here soon.",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(MediHelpSpacing.space6),
        )
    }

    if (showLogoutConfirmation) {
        MediHelpConfirmationDialog(
            title = "Log out?",
            message = "You'll need to log in again to see your health information.",
            confirmLabel = "Log out",
            onConfirm = {
                showLogoutConfirmation = false
                viewModel.logout()
            },
            onDismiss = { showLogoutConfirmation = false },
        )
    }
}
