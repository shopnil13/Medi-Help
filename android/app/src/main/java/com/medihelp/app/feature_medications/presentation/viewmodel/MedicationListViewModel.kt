package com.medihelp.app.feature_medications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import com.medihelp.app.feature_medications.presentation.state.MedicationListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MedicationListViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
) : ViewModel() {

    val uiState: StateFlow<MedicationListUiState> = medicationRepository
        .observeActiveMedications()
        .map { medications -> MedicationListUiState(medications = medications, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MedicationListUiState(),
        )

    init {
        viewModelScope.launch {
            medicationRepository.refreshFromBackend()
        }
    }
}
