package com.medihelp.app.feature_vitals.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_vitals.domain.model.NewVitalInput
import com.medihelp.app.feature_vitals.domain.model.VitalMetricType
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import com.medihelp.app.feature_vitals.presentation.state.AddVitalKind
import com.medihelp.app.feature_vitals.presentation.state.AddVitalUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddVitalViewModel @Inject constructor(
    private val repository: VitalRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddVitalUiState())
    val uiState: StateFlow<AddVitalUiState> = _uiState.asStateFlow()

    fun selectKind(kind: AddVitalKind) {
        val unit = when (kind) {
            AddVitalKind.BLOOD_PRESSURE -> "mmHg"
            AddVitalKind.HEART_RATE -> "bpm"
            AddVitalKind.BLOOD_GLUCOSE -> "mg/dL"
            AddVitalKind.WEIGHT -> "kg"
            AddVitalKind.CUSTOM -> ""
        }
        _uiState.update {
            it.copy(
                kind = kind,
                primaryValue = "",
                secondaryValue = "",
                customName = "",
                unit = unit,
                errorMessage = null,
            )
        }
    }

    fun onPrimaryValueChange(value: String) = _uiState.update {
        it.copy(primaryValue = value, errorMessage = null)
    }

    fun onSecondaryValueChange(value: String) = _uiState.update {
        it.copy(secondaryValue = value, errorMessage = null)
    }

    fun onCustomNameChange(value: String) = _uiState.update {
        it.copy(customName = value, errorMessage = null)
    }

    fun onUnitChange(value: String) = _uiState.update { it.copy(unit = value, errorMessage = null) }

    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun onRecordedAtChange(value: Instant) = _uiState.update { it.copy(recordedAt = value) }

    fun save() {
        val state = _uiState.value
        val inputs = state.toInputsOrNull()
        if (inputs == null) {
            _uiState.update { it.copy(errorMessage = validationMessage(state)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = repository.addVitals(inputs)) {
                is Result.Success -> _uiState.update {
                    it.copy(isSaving = false, saveSucceeded = true)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun AddVitalUiState.toInputsOrNull(): List<NewVitalInput>? {
        val primary = primaryValue.toPositiveDoubleOrNull() ?: return null
        val notesValue = notes.trim().ifBlank { null }
        return when (kind) {
            AddVitalKind.BLOOD_PRESSURE -> {
                val secondary = secondaryValue.toPositiveDoubleOrNull() ?: return null
                listOf(
                    newInput(VitalMetricType.BLOOD_PRESSURE_SYSTOLIC, primary, "mmHg", notesValue),
                    newInput(VitalMetricType.BLOOD_PRESSURE_DIASTOLIC, secondary, "mmHg", notesValue),
                )
            }
            AddVitalKind.HEART_RATE -> listOf(
                newInput(VitalMetricType.HEART_RATE, primary, "bpm", notesValue),
            )
            AddVitalKind.BLOOD_GLUCOSE -> listOf(
                newInput(VitalMetricType.BLOOD_GLUCOSE, primary, unit, notesValue),
            )
            AddVitalKind.WEIGHT -> listOf(
                newInput(VitalMetricType.WEIGHT, primary, unit, notesValue),
            )
            AddVitalKind.CUSTOM -> {
                if (customName.isBlank() || unit.isBlank()) return null
                listOf(
                    newInput(
                        VitalMetricType.CUSTOM,
                        primary,
                        unit.trim(),
                        notesValue,
                        customName.trim(),
                    ),
                )
            }
        }
    }

    private fun AddVitalUiState.newInput(
        type: VitalMetricType,
        value: Double,
        unit: String,
        notes: String?,
        name: String? = null,
    ) = NewVitalInput(
        metricType = type,
        metricName = name,
        value = value,
        unit = unit,
        recordedAt = recordedAt,
        notes = notes,
    )
}

private fun String.toPositiveDoubleOrNull(): Double? =
    toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }

private fun validationMessage(state: AddVitalUiState): String = when {
    state.kind == AddVitalKind.CUSTOM && state.customName.isBlank() -> "Enter a marker name."
    state.unit.isBlank() -> "Enter a unit."
    state.kind == AddVitalKind.BLOOD_PRESSURE -> "Enter valid systolic and diastolic values."
    else -> "Enter a valid value."
}

