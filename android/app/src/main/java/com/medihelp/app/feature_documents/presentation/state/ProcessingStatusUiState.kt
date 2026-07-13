package com.medihelp.app.feature_documents.presentation.state

import com.medihelp.app.feature_documents.domain.model.UploadedDocument

data class ProcessingStatusUiState(
    val document: UploadedDocument? = null,
    val isLoading: Boolean = true,
    val refreshError: String? = null,
)
