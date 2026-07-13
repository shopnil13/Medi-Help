package com.medihelp.app.feature_documents.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.medihelp.app.MainDispatcherRule
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_documents.domain.model.ExtractedMedication
import com.medihelp.app.feature_documents.domain.model.ExtractionResult
import com.medihelp.app.feature_documents.domain.model.PrescriptionExtraction
import com.medihelp.app.feature_documents.domain.model.UploadedDocument
import com.medihelp.app.feature_documents.domain.repository.DocumentRepository
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
        val viewModel = ExtractionReviewViewModel(
            SavedStateHandle(mapOf("jobId" to "job-1")),
            repository,
        )
        advanceUntilIdle()

        viewModel.updateMedication(0) { it.copy(dosage = "1 tablet", selected = true) }
        viewModel.confirm()
        advanceUntilIdle()

        val confirmed = repository.confirmed as PrescriptionExtraction
        assertEquals("1 tablet", confirmed.medications.single().dosage)
        assertTrue(viewModel.uiState.value.isConfirmed)
    }

    private fun document(result: ExtractionResult) = UploadedDocument(
        id = "document-1",
        jobId = "job-1",
        documentType = "prescription",
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
