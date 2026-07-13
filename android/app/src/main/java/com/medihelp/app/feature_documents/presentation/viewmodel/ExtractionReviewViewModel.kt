package com.medihelp.app.feature_documents.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_documents.domain.model.ExtractedBiomarker
import com.medihelp.app.feature_documents.domain.model.ExtractedMedication
import com.medihelp.app.feature_documents.domain.model.LabReportExtraction
import com.medihelp.app.feature_documents.domain.model.PrescriptionExtraction
import com.medihelp.app.feature_documents.domain.repository.DocumentRepository
import com.medihelp.app.feature_documents.presentation.state.ExtractionReviewUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExtractionReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DocumentRepository,
) : ViewModel() {
    private val jobId: String = checkNotNull(savedStateHandle["jobId"])
    private val _uiState = MutableStateFlow(ExtractionReviewUiState())
    val uiState: StateFlow<ExtractionReviewUiState> = _uiState.asStateFlow()
    private var draftInitialized = false

    init {
        viewModelScope.launch {
            repository.observeJob(jobId).collect { document ->
                val extracted = document?.structuredResult
                if (!draftInitialized && extracted != null) {
                    draftInitialized = true
                    _uiState.update { it.copy(document = document, draft = extracted, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            document = document,
                            isLoading = document == null,
                            isConfirmed = document?.confirmedResult != null || it.isConfirmed,
                        )
                    }
                }
            }
        }
    }

    fun updateMedication(index: Int, transform: (ExtractedMedication) -> ExtractedMedication) {
        _uiState.update { state ->
            val draft = state.draft as? PrescriptionExtraction ?: return@update state
            state.copy(
                draft = draft.copy(
                    medications = draft.medications.mapIndexed { itemIndex, item ->
                        if (itemIndex == index) transform(item) else item
                    },
                ),
                errorMessage = null,
            )
        }
    }

    fun updateBiomarker(index: Int, transform: (ExtractedBiomarker) -> ExtractedBiomarker) {
        _uiState.update { state ->
            val draft = state.draft as? LabReportExtraction ?: return@update state
            state.copy(
                draft = draft.copy(
                    biomarkers = draft.biomarkers.mapIndexed { itemIndex, item ->
                        if (itemIndex == index) transform(item) else item
                    },
                ),
                errorMessage = null,
            )
        }
    }

    fun confirm() {
        val draft = _uiState.value.draft ?: return
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.confirmExtraction(jobId, draft)) {
                is Result.Success -> _uiState.update {
                    it.copy(isSaving = false, isConfirmed = true)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }
}
