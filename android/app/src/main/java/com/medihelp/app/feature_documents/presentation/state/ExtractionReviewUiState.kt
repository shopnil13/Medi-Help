package com.medihelp.app.feature_documents.presentation.state

import com.medihelp.app.feature_documents.domain.model.ExtractionResult
import com.medihelp.app.feature_documents.domain.model.UploadedDocument

data class ExtractionReviewUiState(
    val document: UploadedDocument? = null,
    val draft: ExtractionResult? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isConfirmed: Boolean = false,
)
