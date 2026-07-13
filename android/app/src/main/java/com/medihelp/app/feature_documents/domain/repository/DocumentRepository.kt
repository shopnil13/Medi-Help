package com.medihelp.app.feature_documents.domain.repository

import android.net.Uri
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_documents.domain.model.UploadedDocument
import com.medihelp.app.feature_documents.domain.model.ExtractionResult
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeJob(jobId: String): Flow<UploadedDocument?>
    fun observeDocuments(): Flow<List<UploadedDocument>>
    suspend fun uploadDocument(uri: Uri, documentType: String): Result<String>
    suspend fun refreshJob(jobId: String): Result<Unit>
    suspend fun confirmExtraction(jobId: String, result: ExtractionResult): Result<Unit>
}
