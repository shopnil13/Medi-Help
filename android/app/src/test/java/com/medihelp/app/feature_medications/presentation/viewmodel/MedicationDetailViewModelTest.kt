package com.medihelp.app.feature_medications.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.medihelp.app.MainDispatcherRule
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_medications.domain.model.Medication
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import com.medihelp.app.feature_medications.domain.model.NewMedicationInput
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun requestsAndExposesCachedSimplification() = runTest {
        val repository = MedicationRepositoryFake()
        val viewModel = MedicationDetailViewModel(
            repository,
            SavedStateHandle(mapOf("medicationId" to "med-1")),
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()

        assertEquals("med-1", repository.simplifiedId)
        assertEquals("Helps control blood sugar.", viewModel.uiState.value.medication?.purposeSimplified)
        assertFalse(viewModel.uiState.value.isSimplifying)
    }
}

private class MedicationRepositoryFake : MedicationRepository {
    private val medication = MutableStateFlow(sampleMedication())
    var simplifiedId: String? = null

    override fun observeActiveMedications(): Flow<List<Medication>> = emptyFlow()
    override fun observeMedication(id: String): Flow<Medication?> = medication
    override suspend fun addMedication(input: NewMedicationInput): Result<Unit> = Result.Error("Unused")
    override suspend fun updateStatus(id: String, status: MedicationStatus): Result<Unit> =
        Result.Error("Unused")
    override suspend fun deleteMedication(id: String): Result<Unit> = Result.Error("Unused")
    override suspend fun logReminderAction(medicationId: String, scheduledAt: Instant, action: String) = Unit
    override suspend fun refreshFromBackend() = Unit
    override suspend fun syncPendingChanges() = Unit
    override suspend fun importConfirmedPrescription(jobId: String): Result<List<Medication>> =
        Result.Error("Unused")

    override suspend fun simplifyMedication(id: String): Result<Unit> {
        simplifiedId = id
        medication.value = sampleMedication().copy(
            purposeSimplified = "Helps control blood sugar.",
            simplifiedInstruction = "Take one tablet after breakfast.",
        )
        return Result.Success(Unit)
    }
}

private fun sampleMedication() = Medication(
    id = "med-1",
    name = "Metformin",
    strength = "500 mg",
    dosageInstruction = "Take 1 tablet after breakfast",
    simplifiedInstruction = null,
    purposeSimplified = null,
    startDate = null,
    endDate = null,
    status = MedicationStatus.ACTIVE,
    requiresReview = false,
    isSynced = true,
    schedules = emptyList(),
)
