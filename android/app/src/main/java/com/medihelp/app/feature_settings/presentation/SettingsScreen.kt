package com.medihelp.app.feature_settings.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.R
import com.medihelp.app.core.designsystem.components.MediHelpConfirmationDialog
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpRadius
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.core.designsystem.theme.Red600
import com.medihelp.app.core.designsystem.theme.Red800

private val AvatarSize = 56.dp

/**
 * Settings intentionally lists only actions that are actually wired up. The
 * design also sketches accessibility, app-lock and biometric toggles; those
 * have no implementation behind them yet, and a switch that silently does
 * nothing is worse than an absent one in a health app.
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onHealthConnectClick: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showDisclaimer by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.hasLoggedOut) {
        if (uiState.hasLoggedOut) {
            onLoggedOut()
        }
    }

    Scaffold(
        topBar = {
            MediHelpTopBar(
                title = stringResource(R.string.settings_title),
                onBackClick = onBackClick,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(MediHelpSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
        ) {
            ProfileCard(
                displayName = uiState.displayName,
                emailAddress = uiState.emailAddress,
            )

            SettingsSection(stringResource(R.string.settings_section_reminders)) {
                SettingsRow(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.settings_notification_settings),
                    subtitle = stringResource(R.string.settings_notification_settings_subtitle),
                    onClick = { context.openAppNotificationSettings() },
                )
            }

            SettingsSection(stringResource(R.string.settings_section_health_connect)) {
                SettingsRow(
                    icon = Icons.Outlined.MonitorHeart,
                    title = stringResource(R.string.settings_health_connect),
                    subtitle = stringResource(R.string.settings_health_connect_subtitle),
                    onClick = onHealthConnectClick,
                )
            }

            SettingsSection(stringResource(R.string.settings_section_about)) {
                SettingsRow(
                    icon = Icons.Outlined.HealthAndSafety,
                    title = stringResource(R.string.settings_medical_disclaimer),
                    onClick = { showDisclaimer = true },
                )
            }

            SettingsSection(stringResource(R.string.settings_section_account)) {
                SettingsRow(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = stringResource(R.string.settings_log_out),
                    tint = Red800,
                    onClick = { showLogoutConfirmation = true },
                )
            }
        }
    }

    if (showLogoutConfirmation) {
        MediHelpConfirmationDialog(
            title = stringResource(R.string.settings_log_out_confirm_title),
            message = stringResource(R.string.settings_log_out_confirm_message),
            confirmLabel = stringResource(R.string.settings_log_out),
            onConfirm = {
                showLogoutConfirmation = false
                viewModel.logout()
            },
            onDismiss = { showLogoutConfirmation = false },
        )
    }

    if (showDisclaimer) {
        MediHelpConfirmationDialog(
            title = stringResource(R.string.settings_medical_disclaimer),
            message = stringResource(R.string.medical_disclaimer_body),
            confirmLabel = stringResource(android.R.string.ok),
            onConfirm = { showDisclaimer = false },
            onDismiss = { showDisclaimer = false },
        )
    }
}

@Composable
private fun ProfileCard(
    displayName: String?,
    emailAddress: String?,
) {
    val name = displayName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.dashboard_greeting_fallback_name)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MediHelpRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(MediHelpSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(AvatarSize)
                    .background(color = Red600, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.first().uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(Modifier.size(MediHelpSpacing.space3))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleLarge)
                if (emailAddress != null) {
                    Text(
                        text = emailAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space2)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Red800,
            modifier = Modifier.padding(start = MediHelpSpacing.space2),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(MediHelpRadius.lg),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    tint: Color = Red600,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MediHelpSpacing.tapTargetMin)
            .clickable(onClick = onClick)
            .padding(MediHelpSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Spacer(Modifier.size(MediHelpSpacing.space3))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(MediHelpSpacing.space4),
        )
    }
}

/**
 * Opens this app's notification settings. Falls back to the app details page on
 * devices whose OEM skin does not honour the per-app notification intent.
 */
private fun Context.openAppNotificationSettings() {
    val notificationSettings = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(android.net.Uri.fromParts("package", packageName, null))

    try {
        startActivity(notificationSettings)
    } catch (error: ActivityNotFoundException) {
        startActivity(appDetails)
    }
}
