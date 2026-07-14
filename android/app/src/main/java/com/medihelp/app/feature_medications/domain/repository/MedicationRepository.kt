package com.medihelp.app.feature_medications.domain.repository

import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_medications.domain.model.Medication
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import com.medihelp.app.feature_medications.domain.model.NewMedicationInput
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeActiveMedications(): Flow<List<Medication>>
    fun observeMedication(id: String): Flow<Medication?>
    suspend fun addMedication(input: NewMedicationInput): Result<Unit>
    suspend fun updateStatus(id: String, status: MedicationStatus): Result<Unit>
    suspend fun deleteMedication(id: String): Result<Unit>
    suspend fun logReminderAction(medicationId: String, scheduledAt: Instant, action: String)
    suspend fun refreshFromBackend()
    suspend fun syncPendingChanges()
    suspend fun importConfirmedPrescription(jobId: String): Result<List<Medication>>
}
