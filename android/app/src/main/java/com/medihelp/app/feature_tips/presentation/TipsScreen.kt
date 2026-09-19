package com.medihelp.app.feature_tips.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.medihelp.app.R
import com.medihelp.app.core.designsystem.theme.MediHelpRadius
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.core.designsystem.theme.StatusSuccess

private val TipResources = listOf(
    R.string.tips_water,
    R.string.tips_routine,
    R.string.tips_records,
    R.string.tips_movement,
    R.string.tips_food,
    R.string.tips_sleep,
    R.string.tips_questions,
)

/**
 * General wellbeing tips.
 *
 * These are the same fixed suggestions for everyone. Tips drawn from the user's
 * own adherence and vitals arrive with the medical history analyser; until then
 * nothing here is personalised, and the disclaimer says so.
 */
@Composable
fun TipsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(MediHelpSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
    ) {
        Text(
            text = stringResource(R.string.tips_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.tips_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TipResources.forEach { tipRes ->
            TipCard(stringResource(tipRes))
        }

        Text(
            text = stringResource(R.string.dashboard_tip_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MediHelpSpacing.space2),
        )
    }
}

@Composable
private fun TipCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MediHelpRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.padding(MediHelpSpacing.space4)) {
            Icon(
                imageVector = Icons.Outlined.Spa,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(MediHelpSpacing.space3))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
