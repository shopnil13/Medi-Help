package com.medihelp.app.feature_healthconnect.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_healthconnect.data.HealthConnectRepositoryImpl
import com.medihelp.app.feature_healthconnect.domain.model.HealthConnectAvailability

@Composable
fun HealthConnectScreen(
    onBackClick: () -> Unit,
    viewModel: HealthConnectViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { grantedPermissions ->
        viewModel.onPermissionResult(
            grantedPermissions.containsAll(viewModel.requiredPermissions),
        )
    }

    LaunchedEffect(state.shouldRequestPermissions) {
        if (state.shouldRequestPermissions) {
            viewModel.onPermissionRequestLaunched()
            permissionLauncher.launch(viewModel.requiredPermissions)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = { MediHelpTopBar(title = "Health Connect", onBackClick = onBackClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                start = MediHelpSpacing.space4,
                top = MediHelpSpacing.space4,
                end = MediHelpSpacing.space4,
                bottom = MediHelpSpacing.space8,
            ),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Watch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space1),
                    ) {
                        Text("Connected health data", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = statusText(state),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (state.availability == HealthConnectAvailability.AVAILABLE) {
                item {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = MediHelpSpacing.space3),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sync health records", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Heart rate, blood pressure, and blood glucose",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.syncEnabled,
                            onCheckedChange = viewModel::setSyncEnabled,
                            enabled = !state.isChecking && !state.isSyncing,
                        )
                    }
                    HorizontalDivider()
                }

                if (state.syncEnabled && state.hasPermissions) {
                    item {
                        Button(
                            onClick = viewModel::syncNow,
                            enabled = !state.isSyncing,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                            }
                            Text(
                                text = if (state.isSyncing) "Syncing" else "Sync now",
                                modifier = Modifier.padding(start = MediHelpSpacing.space2),
                            )
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { openHealthConnectSettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.HealthAndSafety, contentDescription = null)
                        Text(
                            "Manage access",
                            modifier = Modifier.padding(start = MediHelpSpacing.space2),
                        )
                    }
                }
            }

            if (state.availability == HealthConnectAvailability.UPDATE_REQUIRED) {
                item {
                    Button(
                        onClick = { openHealthConnectStore(context) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                        Text(
                            "Update Health Connect",
                            modifier = Modifier.padding(start = MediHelpSpacing.space2),
                        )
                    }
                }
            }

            state.lastImportedCount?.let { count ->
                item {
                    Text(
                        text = "$count health ${if (count == 1) "record" else "records"} imported.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            state.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun statusText(state: HealthConnectUiState): String = when {
    state.isChecking -> "Checking availability"
    state.availability == HealthConnectAvailability.UNAVAILABLE ->
        "Health Connect is unavailable on this device."
    state.availability == HealthConnectAvailability.UPDATE_REQUIRED ->
        "Health Connect needs an update."
    state.syncEnabled && state.hasPermissions -> "Sync is on."
    state.hasPermissions -> "Access granted. Sync is off."
    else -> "Not connected."
}

private fun openHealthConnectSettings(context: Context) {
    context.startActivity(
        HealthConnectClient.getHealthConnectManageDataIntent(
            context,
            HealthConnectRepositoryImpl.PROVIDER_PACKAGE_NAME,
        ),
    )
}

private fun openHealthConnectStore(context: Context) {
    val packageName = HealthConnectRepositoryImpl.PROVIDER_PACKAGE_NAME
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    try {
        context.startActivity(marketIntent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
            ),
        )
    }
}
