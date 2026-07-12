package com.medihelp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.medihelp.app.core.designsystem.theme.MediHelpTheme
import com.medihelp.app.core.navigation.AppNavGraph
import com.medihelp.app.core.navigation.Routes
import com.medihelp.app.feature_auth.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = if (authRepository.isLoggedIn()) {
            Routes.DASHBOARD
        } else {
            Routes.ONBOARDING
        }

        setContent {
            MediHelpTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
