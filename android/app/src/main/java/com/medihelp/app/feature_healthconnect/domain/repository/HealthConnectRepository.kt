package com.medihelp.app.feature_healthconnect.domain.repository

import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_healthconnect.domain.model.HealthConnectAvailability
import kotlinx.coroutines.flow.Flow

interface HealthConnectRepository {
    val requiredPermissions: Set<String>

    fun observeSyncEnabled(): Flow<Boolean>
    fun availability(): HealthConnectAvailability
    suspend fun hasAllPermissions(): Boolean
    suspend fun setSyncEnabled(enabled: Boolean)
    suspend fun importRecentRecords(): Result<Int>
}
