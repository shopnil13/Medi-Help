package com.medihelp.app.feature_medications.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.feature_medications.domain.model.Medication
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import com.medihelp.app.core.common.Result
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
    val isSimplifying: Boolean = true,
    val simplificationError: String? = null,
)

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val medicationId: String = checkNotNull(savedStateHandle["medicationId"])
    private val isDeleted = MutableStateFlow(false)
    private val simplificationState = MutableStateFlow<Pair<Boolean, String?>>(true to null)

    val uiState: StateFlow<MedicationDetailUiState> = medicationRepository
        .observeMedication(medicationId)
        .combine(isDeleted) { medication, deleted -> medication to deleted }
        .combine(simplificationState) { medicationState, simplification ->
            MedicationDetailUiState(
                medication = medicationState.first,
                isDeleted = medicationState.second,
                isSimplifying = simplification.first,
                simplificationError = simplification.second,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MedicationDetailUiState(),
        )

    init {
        viewModelScope.launch {
            when (val result = medicationRepository.simplifyMedication(medicationId)) {
                is Result.Success -> simplificationState.value = false to null
                is Result.Error -> simplificationState.value = false to result.message
            }
        }
    }

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
