package com.medihelp.app.feature_documents.domain.model

import java.time.Instant

data class UploadedDocument(
    val id: String,
    val jobId: String,
    val documentType: String,
    val originalFilename: String,
    val contentType: String,
    val fileSizeBytes: Long,
    val documentStatus: String,
    val jobStatus: String,
    val progressPercent: Int,
    val errorMessage: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
