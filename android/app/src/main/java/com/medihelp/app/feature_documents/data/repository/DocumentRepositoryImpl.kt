package com.medihelp.app.feature_documents.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.medihelp.app.core.common.Result
import com.medihelp.app.core.common.toUserMessage
import com.medihelp.app.core.database.dao.DocumentDao
import com.medihelp.app.feature_documents.data.mapper.toDomain
import com.medihelp.app.feature_documents.data.mapper.toEntity
import com.medihelp.app.feature_documents.data.remote.DocumentApi
import com.medihelp.app.feature_documents.domain.model.UploadedDocument
import com.medihelp.app.feature_documents.domain.repository.DocumentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val documentApi: DocumentApi,
    private val documentDao: DocumentDao,
) : DocumentRepository {
    private val contentResolver: ContentResolver = context.contentResolver

    override fun observeJob(jobId: String): Flow<UploadedDocument?> =
        documentDao.observeByJobId(jobId).map { it?.toDomain() }

    override fun observeDocuments(): Flow<List<UploadedDocument>> =
        documentDao.observeAll().map { documents -> documents.map { it.toDomain() } }

    override suspend fun uploadDocument(uri: Uri, documentType: String): Result<String> {
        return try {
            val metadata = readMetadata(uri)
            val bytes = contentResolver.openInputStream(uri)?.use(::readBytesWithinLimit)
                ?: throw IOException("The selected document could not be opened.")
            val mediaType = metadata.contentType.toMediaType()
            val part = MultipartBody.Part.createFormData(
                "file",
                metadata.filename,
                bytes.toRequestBody(mediaType),
            )
            val response = documentApi.uploadDocument(
                documentType.toRequestBody("text/plain".toMediaType()),
                part,
            )
            documentDao.upsert(response.job.toEntity())
            Result.Success(response.job.id)
        } catch (error: Exception) {
            Result.Error(error.toUserMessage())
        }
    }

    override suspend fun refreshJob(jobId: String): Result<Unit> {
        return try {
            documentDao.upsert(documentApi.getJob(jobId).toEntity())
            Result.Success(Unit)
        } catch (error: Exception) {
            Result.Error(error.toUserMessage())
        }
    }

    private fun readMetadata(uri: Uri): FileMetadata {
        var filename: String? = null
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                filename = cursor.getString(0)
            }
        }
        val reportedContentType = contentResolver.getType(uri)
        val contentType = reportedContentType?.takeIf { it in SUPPORTED_CONTENT_TYPES }
            ?: when (filename?.substringAfterLast('.', "")?.lowercase()) {
                "pdf" -> "application/pdf"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                else -> throw IOException("Only PDF, JPEG, and PNG documents are supported.")
            }
        return FileMetadata(filename ?: "document", contentType)
    }

    private fun readBytesWithinLimit(input: java.io.InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > MAX_UPLOAD_BYTES) {
                throw IOException("The selected document is larger than 10 MB.")
            }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private data class FileMetadata(val filename: String, val contentType: String)

    private companion object {
        const val MAX_UPLOAD_BYTES = 10 * 1024 * 1024
        val SUPPORTED_CONTENT_TYPES = setOf("application/pdf", "image/jpeg", "image/png")
    }
}
