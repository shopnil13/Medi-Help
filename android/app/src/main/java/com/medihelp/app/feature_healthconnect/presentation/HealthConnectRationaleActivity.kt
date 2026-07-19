package com.medihelp.app.feature_healthconnect.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.core.designsystem.theme.MediHelpTheme

class HealthConnectRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediHelpTheme {
                Scaffold(
                    topBar = {
                        MediHelpTopBar(
                            title = "Health data access",
                            onBackClick = ::finish,
                        )
                    },
                ) { padding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(MediHelpSpacing.space4),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HealthAndSafety,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "Medi-Help reads heart rate, blood pressure, and blood glucose records you approve in Health Connect.",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "Approved records are stored in Medi-Help and synced to your account so they appear in your health chart. Medi-Help does not write data to Health Connect.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "You can pause syncing in Medi-Help or change access at any time in Health Connect.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
