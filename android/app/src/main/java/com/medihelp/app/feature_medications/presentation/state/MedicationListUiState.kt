package com.medihelp.app.feature_medications.presentation.state

import com.medihelp.app.feature_medications.domain.model.Medication

data class MedicationListUiState(
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = true,
)
