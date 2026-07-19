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
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
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
    private val medicationRepository: MedicationRepository,
    private val vitalRepository: VitalRepository,
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
                            extractionConfirmed = document?.confirmedResult != null || it.extractionConfirmed,
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
            if (!_uiState.value.extractionConfirmed) {
                when (val confirmation = repository.confirmExtraction(jobId, draft)) {
                    is Result.Success -> _uiState.update { it.copy(extractionConfirmed = true) }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = confirmation.message)
                        }
                        return@launch
                    }
                }
            }

            if (draft is PrescriptionExtraction) {
                when (val imported = medicationRepository.importConfirmedPrescription(jobId)) {
                    is Result.Success -> _uiState.update {
                        it.copy(
                            isSaving = false,
                            isConfirmed = true,
                            importedMedications = imported.data,
                        )
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isSaving = false, errorMessage = imported.message)
                    }
                }
            } else if (draft is LabReportExtraction) {
                when (val imported = vitalRepository.importConfirmedLab(jobId)) {
                    is Result.Success -> _uiState.update {
                        it.copy(
                            isSaving = false,
                            isConfirmed = true,
                            importedLabRecords = imported.data,
                        )
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isSaving = false, errorMessage = imported.message)
                    }
                }
            }
        }
    }

    fun setReminderEnabled(medicationId: String, enabled: Boolean) {
        viewModelScope.launch {
            val status = if (enabled) MedicationStatus.ACTIVE else MedicationStatus.PAUSED
            when (val result = medicationRepository.updateStatus(medicationId, status)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(
                        importedMedications = state.importedMedications.map { medication ->
                            if (medication.id == medicationId) medication.copy(status = status) else medication
                        },
                        errorMessage = null,
                    )
                }
                is Result.Error -> _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }
}
