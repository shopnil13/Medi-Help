package com.medihelp.app.feature_documents.presentation.state

import com.medihelp.app.feature_documents.domain.model.ExtractionResult
import com.medihelp.app.feature_documents.domain.model.UploadedDocument
import com.medihelp.app.feature_medications.domain.model.Medication
import com.medihelp.app.feature_vitals.domain.model.VitalRecord

data class ExtractionReviewUiState(
    val document: UploadedDocument? = null,
    val draft: ExtractionResult? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isConfirmed: Boolean = false,
    val extractionConfirmed: Boolean = false,
    val importedMedications: List<Medication> = emptyList(),
    val importedLabRecords: List<VitalRecord> = emptyList(),
)
