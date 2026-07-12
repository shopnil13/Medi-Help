package com.medihelp.app.feature_medications.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.feature_medications.domain.model.Medication
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MedicationDetailUiState(
    val medication: Medication? = null,
    val isDeleted: Boolean = false,
)

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val medicationId: String = checkNotNull(savedStateHandle["medicationId"])
    private val isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<MedicationDetailUiState> = medicationRepository
        .observeMedication(medicationId)
        .combine(isDeleted) { medication, deleted ->
            MedicationDetailUiState(medication = medication, isDeleted = deleted)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MedicationDetailUiState(),
        )

    fun togglePause() {
        val current = uiState.value.medication ?: return
        val newStatus = if (current.status == MedicationStatus.ACTIVE) {
            MedicationStatus.PAUSED
        } else {
            MedicationStatus.ACTIVE
        }
        viewModelScope.launch {
            medicationRepository.updateStatus(medicationId, newStatus)
        }
    }

    fun delete() {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medicationId)
            isDeleted.value = true
        }
    }
}
