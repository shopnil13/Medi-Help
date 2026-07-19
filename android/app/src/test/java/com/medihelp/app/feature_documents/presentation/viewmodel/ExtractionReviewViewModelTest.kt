package com.medihelp.app.feature_documents.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.medihelp.app.MainDispatcherRule
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_documents.domain.model.ExtractedMedication
import com.medihelp.app.feature_documents.domain.model.ExtractedBiomarker
import com.medihelp.app.feature_documents.domain.model.ExtractionResult
import com.medihelp.app.feature_documents.domain.model.LabReportExtraction
import com.medihelp.app.feature_documents.domain.model.PrescriptionExtraction
import com.medihelp.app.feature_documents.domain.model.UploadedDocument
import com.medihelp.app.feature_documents.domain.repository.DocumentRepository
import com.medihelp.app.feature_medications.domain.model.Medication
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import com.medihelp.app.feature_medications.domain.model.NewMedicationInput
import com.medihelp.app.feature_vitals.domain.model.NewVitalInput
import com.medihelp.app.feature_vitals.domain.model.VitalMetricType
import com.medihelp.app.feature_vitals.domain.model.VitalRecord
import com.medihelp.app.feature_vitals.domain.model.VitalSource
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExtractionReviewViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun editsSelectionAndConfirmsDraft() = runTest {
        val extracted = PrescriptionExtraction(
            medications = listOf(ExtractedMedication(name = "Metformin", confidence = 0.8)),
            overallConfidence = 0.8,
        )
        val repository = ReviewRepository(document(extracted))
        val medicationRepository = ReviewMedicationRepository()
        val vitalRepository = ReviewVitalRepository()
        val viewModel = ExtractionReviewViewModel(
            SavedStateHandle(mapOf("jobId" to "job-1")),
            repository,
            medicationRepository,
            vitalRepository,
        )
        advanceUntilIdle()

        viewModel.updateMedication(0) { it.copy(dosage = "1 tablet", selected = true) }
        viewModel.confirm()
        advanceUntilIdle()

        val confirmed = repository.confirmed as PrescriptionExtraction
        assertEquals("1 tablet", confirmed.medications.single().dosage)
        assertTrue(viewModel.uiState.value.isConfirmed)
        assertEquals("Metformin", viewModel.uiState.value.importedMedications.single().name)

        viewModel.setReminderEnabled("medication-1", false)
        advanceUntilIdle()
        assertEquals(MedicationStatus.PAUSED, viewModel.uiState.value.importedMedications.single().status)
        assertEquals(MedicationStatus.PAUSED, medicationRepository.updatedStatus)
    }

    @Test
    fun editsAndImportsConfirmedLabIntoHealthChart() = runTest {
        val extracted = LabReportExtraction(
            biomarkers = listOf(
                ExtractedBiomarker(
                    name = "Fasting glucose",
                    value = "108",
                    unit = "mg/dL",
                    referenceRange = "70-99",
                    confidence = 0.9,
                ),
            ),
            overallConfidence = 0.9,
        )
        val repository = ReviewRepository(document(extracted, "lab_report"))
        val vitalRepository = ReviewVitalRepository()
        val viewModel = ExtractionReviewViewModel(
            SavedStateHandle(mapOf("jobId" to "job-1")),
            repository,
            ReviewMedicationRepository(),
            vitalRepository,
        )
        advanceUntilIdle()

        viewModel.updateBiomarker(0) { it.copy(value = "110") }
        viewModel.confirm()
        advanceUntilIdle()

        val confirmed = repository.confirmed as LabReportExtraction
        assertEquals("110", confirmed.biomarkers.single().value)
        assertEquals("job-1", vitalRepository.importedJobId)
        assertTrue(viewModel.uiState.value.isConfirmed)
        assertEquals(VitalSource.LAB_REPORT, viewModel.uiState.value.importedLabRecords.single().source)
    }

    private fun document(
        result: ExtractionResult,
        documentType: String = "prescription",
    ) = UploadedDocument(
        id = "document-1",
        jobId = "job-1",
        documentType = documentType,
        originalFilename = "prescription.pdf",
        contentType = "application/pdf",
        fileSizeBytes = 100,
        documentStatus = "processed",
        jobStatus = "needs_review",
        progressPercent = 100,
        errorMessage = null,
        structuredResult = result,
        confirmedResult = null,
        confirmedAt = null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}

private class ReviewVitalRepository : VitalRepository {
    var importedJobId: String? = null

    override fun observeVitals(): Flow<List<VitalRecord>> = emptyFlow()
    override suspend fun addVitals(inputs: List<NewVitalInput>): Result<Unit> = Result.Error("Not used")
    override suspend fun importConfirmedLab(jobId: String): Result<List<VitalRecord>> {
        importedJobId = jobId
        return Result.Success(
            listOf(
                VitalRecord(
                    id = "vital-1",
                    metricType = VitalMetricType.BLOOD_GLUCOSE,
                    metricName = "Fasting glucose",
                    value = 110.0,
                    unit = "mg/dL",
                    recordedAt = Instant.EPOCH,
                    source = VitalSource.LAB_REPORT,
                    sourceDocumentId = "document-1",
                    sourceJobId = jobId,
                    notes = "Reference range: 70-99",
                    isSynced = true,
                ),
            ),
        )
    }
    override suspend fun refreshFromBackend() = Unit
    override suspend fun syncPendingChanges() = Unit
}

private class ReviewMedicationRepository : MedicationRepository {
    var updatedStatus: MedicationStatus? = null
    override fun observeActiveMedications(): Flow<List<Medication>> = emptyFlow()
    override fun observeMedication(id: String): Flow<Medication?> = emptyFlow()
    override suspend fun addMedication(input: NewMedicationInput): Result<Unit> = Result.Success(Unit)
    override suspend fun updateStatus(id: String, status: MedicationStatus): Result<Unit> {
        updatedStatus = status
        return Result.Success(Unit)
    }
    override suspend fun deleteMedication(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun logReminderAction(medicationId: String, scheduledAt: Instant, action: String) = Unit
    override suspend fun refreshFromBackend() = Unit
    override suspend fun syncPendingChanges() = Unit
    override suspend fun importConfirmedPrescription(jobId: String): Result<List<Medication>> =
        Result.Success(
            listOf(
                Medication(
                    id = "medication-1",
                    name = "Metformin",
                    strength = "500 mg",
                    dosageInstruction = "1 tablet",
                    simplifiedInstruction = "1 tablet",
                    purposeSimplified = null,
                    startDate = null,
                    endDate = null,
                    status = MedicationStatus.ACTIVE,
                    requiresReview = false,
                    isSynced = true,
                    schedules = emptyList(),
                ),
            ),
        )
}

private class ReviewRepository(initial: UploadedDocument) : DocumentRepository {
    private val document = MutableStateFlow<UploadedDocument?>(initial)
    var confirmed: ExtractionResult? = null

    override fun observeJob(jobId: String): Flow<UploadedDocument?> = document
    override fun observeDocuments(): Flow<List<UploadedDocument>> = emptyFlow()
    override suspend fun uploadDocument(uri: Uri, documentType: String): Result<String> =
        Result.Error("Not used")
    override suspend fun refreshJob(jobId: String): Result<Unit> = Result.Success(Unit)
    override suspend fun confirmExtraction(jobId: String, result: ExtractionResult): Result<Unit> {
        confirmed = result
        return Result.Success(Unit)
    }
}
