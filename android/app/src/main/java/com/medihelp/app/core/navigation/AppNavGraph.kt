package com.medihelp.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medihelp.app.feature_auth.presentation.screen.LoginScreen
import com.medihelp.app.feature_auth.presentation.screen.OnboardingScreen
import com.medihelp.app.feature_auth.presentation.screen.RegisterScreen
import com.medihelp.app.feature_dashboard.presentation.screen.DashboardScreen

@Composable
fun AppNavGraph(
    startDestination: String,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onLoginClick = { navController.navigate(Routes.LOGIN) },
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginSuccess = { navController.navigateToDashboard() },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigateToDashboard() },
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onLoggedOut = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

private fun NavHostController.navigateToDashboard() {
    navigate(Routes.DASHBOARD) {
        popUpTo(0) { inclusive = true }
    }
}
