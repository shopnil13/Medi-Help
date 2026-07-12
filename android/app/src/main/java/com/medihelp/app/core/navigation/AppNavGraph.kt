package com.medihelp.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medihelp.app.core.designsystem.components.BottomNavTab
import com.medihelp.app.core.designsystem.components.MediHelpBottomNavBar
import com.medihelp.app.feature_auth.presentation.screen.LoginScreen
import com.medihelp.app.feature_auth.presentation.screen.OnboardingScreen
import com.medihelp.app.feature_auth.presentation.screen.RegisterScreen
import com.medihelp.app.feature_dashboard.presentation.screen.DashboardScreen
import com.medihelp.app.feature_medications.presentation.screen.AddMedicationScreen
import com.medihelp.app.feature_medications.presentation.screen.MedicationDetailScreen
import com.medihelp.app.feature_medications.presentation.screen.MedicationListScreen

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
            MainTabScaffold(navController = navController, selectedTab = BottomNavTab.HOME) {
                DashboardScreen(
                    onLoggedOut = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onViewMedicinesClick = {
                        navController.navigate(Routes.MEDICATIONS) {
                            popUpTo(Routes.DASHBOARD)
                        }
                    },
                )
            }
        }

        composable(Routes.MEDICATIONS) {
            MainTabScaffold(navController = navController, selectedTab = BottomNavTab.MEDICINES) {
                MedicationListScreen(
                    onAddMedicationClick = { navController.navigate(Routes.ADD_MEDICATION) },
                    onMedicationClick = { id -> navController.navigate(Routes.medicationDetail(id)) },
                )
            }
        }

        composable(Routes.ADD_MEDICATION) {
            AddMedicationScreen(
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.MEDICATION_DETAIL,
            arguments = listOf(navArgument("medicationId") { type = NavType.StringType }),
        ) {
            MedicationDetailScreen(
                onBackClick = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun MainTabScaffold(
    navController: NavHostController,
    selectedTab: BottomNavTab,
    content: @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = {
            MediHelpBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    val route = when (tab) {
                        BottomNavTab.HOME -> Routes.DASHBOARD
                        BottomNavTab.MEDICINES -> Routes.MEDICATIONS
                    }
                    if (tab != selectedTab) {
                        navController.navigate(route) {
                            popUpTo(Routes.DASHBOARD) { inclusive = route == Routes.DASHBOARD }
                            launchSingleTop = true
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            content()
        }
    }
}

private fun NavHostController.navigateToDashboard() {
    navigate(Routes.DASHBOARD) {
        popUpTo(0) { inclusive = true }
    }
}
