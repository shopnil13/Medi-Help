package com.medihelp.app.feature_auth.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medihelp.app.core.designsystem.components.MediHelpPrimaryButton
import com.medihelp.app.core.designsystem.components.MediHelpSecondaryButton
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.core.designsystem.theme.Red200
import com.medihelp.app.core.designsystem.theme.Red600
import com.medihelp.app.core.designsystem.theme.Red900

@Composable
fun OnboardingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(MediHelpSpacing.space8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Medi-Help",
                style = MaterialTheme.typography.displayLarge,
                color = Red600,
            )
            Spacer(Modifier.height(MediHelpSpacing.space3))
            Text(
                text = "Your health, explained simply.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                .background(
                    Brush.verticalGradient(listOf(Red200, Red600, Red900)),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(MediHelpSpacing.space6)),
        ) {
            MediHelpPrimaryButton(text = "Log in", onClick = onLoginClick)
            Spacer(Modifier.height(MediHelpSpacing.space3))
            MediHelpSecondaryButton(text = "Create account", onClick = onRegisterClick)
        }
    }
}
