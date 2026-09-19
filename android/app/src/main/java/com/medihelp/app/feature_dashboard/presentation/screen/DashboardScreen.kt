package com.medihelp.app.feature_dashboard.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medihelp.app.R
import com.medihelp.app.core.designsystem.theme.MediHelpRadius
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.core.designsystem.theme.Red600
import com.medihelp.app.core.designsystem.theme.Red800
import com.medihelp.app.feature_dashboard.domain.DayGreeting
import com.medihelp.app.feature_dashboard.presentation.viewmodel.DashboardViewModel
import com.medihelp.app.feature_vitals.domain.model.VitalReading
import com.medihelp.app.feature_vitals.domain.model.VitalsSummary

private val AvatarSize = 44.dp
private val CardIconSize = 28.dp

@Composable
fun DashboardScreen(
    onLoggedOut: () -> Unit,
    onViewMedicinesClick: () -> Unit,
    onViewVitalsClick: () -> Unit,
    onUploadDocumentClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.hasLoggedOut) {
        if (uiState.hasLoggedOut) {
            onLoggedOut()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(MediHelpSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
    ) {
        DashboardHeader(
            greeting = uiState.greeting,
            displayName = uiState.displayName,
            onNotificationsClick = onNotificationsClick,
            onProfileClick = onProfileClick,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
        ) {
            MedicinesCard(
                dosesDueToday = uiState.dosesDueToday,
                onClick = onViewMedicinesClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            UploadCard(
                onClick = onUploadDocumentClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
        ) {
            VitalsSummaryCard(
                summary = uiState.vitalsSummary,
                onClick = onViewVitalsClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            HealthTipCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    greeting: DayGreeting,
    displayName: String?,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val greetingText = stringResource(
        when (greeting) {
            DayGreeting.MORNING -> R.string.dashboard_greeting_morning
            DayGreeting.AFTERNOON -> R.string.dashboard_greeting_afternoon
            DayGreeting.EVENING -> R.string.dashboard_greeting_evening
        },
    )
    val fullName = displayName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.dashboard_greeting_fallback_name)
    // Greet by first name only, as the mockup does. A full name wraps onto a
    // second line at this type size and pushes the cards off-screen.
    val firstName = fullName.trim().substringBefore(' ')

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greetingText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = firstName,
                style = MaterialTheme.typography.headlineLarge,
                color = Red800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.size(MediHelpSpacing.tapTargetMin),
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = stringResource(R.string.dashboard_notifications),
                tint = Red800,
            )
        }
        Spacer(Modifier.size(MediHelpSpacing.space2))
        ProfileAvatar(name = fullName, onClick = onProfileClick)
    }
}

/**
 * Initial-based avatar. The brand supplies no photography and the design system
 * forbids inventing stock imagery, so the user's initial stands in for a photo.
 */
@Composable
private fun ProfileAvatar(
    name: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(AvatarSize)
            .background(color = Red600, shape = CircleShape),
    ) {
        Text(
            text = name.first().uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun DashboardCard(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val shape = RoundedCornerShape(MediHelpRadius.lg)

    if (onClick == null) {
        Card(modifier = modifier, shape = shape, colors = colors) {
            Column(modifier = Modifier.padding(MediHelpSpacing.space4), content = content)
        }
    } else {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors) {
            Column(modifier = Modifier.padding(MediHelpSpacing.space4), content = content)
        }
    }
}

@Composable
private fun CardIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Red600,
        modifier = Modifier.size(CardIconSize),
    )
}

/**
 * Card heading. These sit in half-width columns where the large accessible type
 * size leaves little room, so breaking is constrained to whole words.
 */
@Composable
private fun CardTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
    )
}

/**
 * Trailing row of a dashboard card: optional caption on the left, affordance
 * arrow on the right. The arrow is decorative — the whole card is the click
 * target, and it already carries the label screen readers announce.
 */
@Composable
private fun CardAction(label: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = Red800,
        )
    }
}

@Composable
private fun MedicinesCard(
    dosesDueToday: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashboardCard(onClick = onClick, modifier = modifier) {
        CardIcon(Icons.Outlined.Medication)
        Spacer(Modifier.height(MediHelpSpacing.space3))
        CardTitle(stringResource(R.string.dashboard_medicines_title))
        Spacer(Modifier.height(MediHelpSpacing.space2))
        Text(
            text = dosesDueToday.toString(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(MediHelpSpacing.space2))
        CardAction(stringResource(R.string.dashboard_medicines_due))
    }
}

@Composable
private fun UploadCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashboardCard(onClick = onClick, modifier = modifier) {
        CardIcon(Icons.Outlined.Description)
        Spacer(Modifier.height(MediHelpSpacing.space3))
        CardTitle(stringResource(R.string.dashboard_upload_title))
        Spacer(Modifier.height(MediHelpSpacing.space2))
        Text(
            text = stringResource(R.string.dashboard_upload_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MediHelpSpacing.space3))
        CardAction()
    }
}

@Composable
private fun VitalsSummaryCard(
    summary: VitalsSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashboardCard(onClick = onClick, modifier = modifier) {
        // Icon above the title, like the other cards: sharing the row with the
        // icon left too little width and broke "Summary" across two lines.
        CardIcon(Icons.Outlined.MonitorHeart)
        Spacer(Modifier.height(MediHelpSpacing.space3))
        CardTitle(stringResource(R.string.dashboard_vitals_title))
        Spacer(Modifier.height(MediHelpSpacing.space3))

        if (summary.hasAnyReading) {
            VitalRow(stringResource(R.string.vital_blood_pressure_short), summary.bloodPressure)
            VitalRow(stringResource(R.string.vital_heart_rate_short), summary.heartRate)
            VitalRow(stringResource(R.string.vital_glucose_short), summary.glucose)
        } else {
            Text(
                text = stringResource(R.string.dashboard_vitals_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(MediHelpSpacing.space3))
        CardAction(stringResource(R.string.dashboard_vitals_view_chart))
    }
}

/**
 * Label above value rather than beside it. In a half-width card the accessible
 * type size leaves roughly 124dp of content width, which is not enough for
 * "Heart Rate" and "72 bpm" on one line — side by side, the label wrapped and
 * collided with the number.
 */
@Composable
private fun VitalRow(
    label: String,
    reading: VitalReading?,
) {
    if (reading == null) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MediHelpSpacing.space3),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = reading.displayValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.size(MediHelpSpacing.space1))
            Text(
                text = reading.unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HealthTipCard(modifier: Modifier = Modifier) {
    DashboardCard(onClick = null, modifier = modifier) {
        CardTitle(stringResource(R.string.dashboard_tip_title))
        Spacer(Modifier.height(MediHelpSpacing.space3))
        Text(
            text = stringResource(R.string.dashboard_tip_default),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(MediHelpSpacing.space3))
        // Required safety hedge: tips must never read as medical advice.
        Text(
            text = stringResource(R.string.dashboard_tip_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
