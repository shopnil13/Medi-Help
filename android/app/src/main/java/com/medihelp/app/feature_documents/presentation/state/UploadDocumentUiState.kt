package com.medihelp.app.feature_documents.presentation.state

import android.net.Uri

data class SelectedDocument(
    val uri: Uri,
    val filename: String,
    val contentType: String,
)

data class UploadDocumentUiState(
    val documentType: String = "prescription",
    val selectedDocument: SelectedDocument? = null,
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
    val uploadedJobId: String? = null,
)
