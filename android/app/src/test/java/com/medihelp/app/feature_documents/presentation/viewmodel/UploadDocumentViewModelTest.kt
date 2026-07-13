package com.medihelp.app.feature_documents.presentation.viewmodel

import android.net.Uri
import com.medihelp.app.MainDispatcherRule
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_documents.domain.model.UploadedDocument
import com.medihelp.app.feature_documents.domain.repository.DocumentRepository
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UploadDocumentViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun successfulUploadExposesJobForNavigation() = runTest {
        val repository = FakeDocumentRepository(Result.Success("job-123"))
        val viewModel = UploadDocumentViewModel(repository)
        val uri = mockk<Uri>()

        viewModel.setDocumentType("lab_report")
        viewModel.selectDocument(uri, "report.pdf", "application/pdf")
        viewModel.upload()
        advanceUntilIdle()

        assertEquals("lab_report", repository.uploadedType)
        assertEquals("job-123", viewModel.uiState.value.uploadedJobId)
        assertFalse(viewModel.uiState.value.isUploading)

        viewModel.consumeUploadedJob()
        assertEquals(null, viewModel.uiState.value.uploadedJobId)
    }

    @Test
    fun failedUploadShowsRepositoryMessage() = runTest {
        val viewModel = UploadDocumentViewModel(FakeDocumentRepository(Result.Error("Upload failed")))
        viewModel.selectDocument(mockk(), "report.pdf", "application/pdf")

        viewModel.upload()
        advanceUntilIdle()

        assertEquals("Upload failed", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isUploading)
    }
}

private class FakeDocumentRepository(
    private val uploadResult: Result<String>,
) : DocumentRepository {
    var uploadedType: String? = null

    override fun observeJob(jobId: String): Flow<UploadedDocument?> = emptyFlow()
    override fun observeDocuments(): Flow<List<UploadedDocument>> = emptyFlow()

    override suspend fun uploadDocument(uri: Uri, documentType: String): Result<String> {
        uploadedType = documentType
        return uploadResult
    }

    override suspend fun refreshJob(jobId: String): Result<Unit> = Result.Success(Unit)
}
