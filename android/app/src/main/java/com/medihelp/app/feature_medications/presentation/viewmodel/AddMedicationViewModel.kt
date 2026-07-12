package com.medihelp.app.feature_medications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_medications.domain.model.NewMedicationInput
import com.medihelp.app.feature_medications.domain.model.NewScheduleInput
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import com.medihelp.app.feature_medications.presentation.state.AddMedicationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onStrengthChange(value: String) {
        _uiState.update { it.copy(strength = value) }
    }

    fun onDosageInstructionChange(value: String) {
        _uiState.update { it.copy(dosageInstruction = value, errorMessage = null) }
    }

    fun onDoseAmountChange(value: String) {
        _uiState.update { it.copy(doseAmount = value) }
    }

    fun onTimeChange(time: LocalTime) {
        _uiState.update { it.copy(timeOfDay = time) }
    }

    fun onMealRelationChange(value: String) {
        _uiState.update { it.copy(mealRelation = value) }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank() || state.dosageInstruction.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Please enter the medicine name and how to take it.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val input = NewMedicationInput(
                name = state.name,
                strength = state.strength.ifBlank { null },
                dosageInstruction = state.dosageInstruction,
                startDate = null,
                endDate = null,
                schedules = listOf(
                    NewScheduleInput(
                        timeOfDay = state.timeOfDay,
                        mealRelation = state.mealRelation,
                        doseAmount = state.doseAmount.ifBlank { null },
                    ),
                ),
            )

            when (val result = medicationRepository.addMedication(input)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, saveSucceeded = true) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}
