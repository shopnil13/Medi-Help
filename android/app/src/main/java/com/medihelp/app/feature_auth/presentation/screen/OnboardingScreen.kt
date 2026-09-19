package com.medihelp.app.feature_auth.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medihelp.app.R
import com.medihelp.app.core.designsystem.components.BrandWaveFooter
import com.medihelp.app.core.designsystem.components.MediHelpPrimaryButton
import com.medihelp.app.core.designsystem.components.MediHelpSecondaryButton
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.core.designsystem.theme.Red900

private val LogoSize = 140.dp
private val WaveFooterHeight = 190.dp

@Composable
fun OnboardingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Decorative only; the brand mark above already names the app.
        BrandWaveFooter(
            modifier = Modifier
                .fillMaxWidth()
                .height(WaveFooterHeight)
                .align(Alignment.BottomCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = MediHelpSpacing.space6),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.ic_medihelp_logo),
                contentDescription = stringResource(R.string.app_logo_description),
                modifier = Modifier.size(LogoSize),
            )
            Spacer(Modifier.height(MediHelpSpacing.space5))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = Red900,
            )
            Spacer(Modifier.height(MediHelpSpacing.space3))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))

            // Actions stay clear of the wave band so the outlined secondary
            // button keeps its contrast against the warm background.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
            ) {
                MediHelpPrimaryButton(
                    text = stringResource(R.string.onboarding_log_in),
                    onClick = onLoginClick,
                )
                MediHelpSecondaryButton(
                    text = stringResource(R.string.onboarding_create_account),
                    onClick = onRegisterClick,
                )
            }
            Spacer(Modifier.height(WaveFooterHeight + MediHelpSpacing.space2))
        }
    }
}
