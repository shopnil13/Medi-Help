package com.medihelp.app.feature_vitals.domain.repository

import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_vitals.domain.model.NewVitalInput
import com.medihelp.app.feature_vitals.domain.model.VitalRecord
import kotlinx.coroutines.flow.Flow

interface VitalRepository {
    fun observeVitals(): Flow<List<VitalRecord>>
    suspend fun addVitals(inputs: List<NewVitalInput>): Result<Unit>
    suspend fun refreshFromBackend()
    suspend fun syncPendingChanges()
}

