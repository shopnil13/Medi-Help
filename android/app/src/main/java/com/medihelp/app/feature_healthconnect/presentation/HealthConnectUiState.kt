package com.medihelp.app.feature_healthconnect.presentation

import com.medihelp.app.feature_healthconnect.domain.model.HealthConnectAvailability

data class HealthConnectUiState(
    val availability: HealthConnectAvailability = HealthConnectAvailability.UNAVAILABLE,
    val syncEnabled: Boolean = false,
    val hasPermissions: Boolean = false,
    val isChecking: Boolean = true,
    val isSyncing: Boolean = false,
    val shouldRequestPermissions: Boolean = false,
    val lastImportedCount: Int? = null,
    val errorMessage: String? = null,
)
