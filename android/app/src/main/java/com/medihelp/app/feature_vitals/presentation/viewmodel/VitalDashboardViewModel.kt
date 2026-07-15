package com.medihelp.app.feature_vitals.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import com.medihelp.app.feature_vitals.presentation.state.VitalDashboardUiState
import com.medihelp.app.feature_vitals.presentation.state.VitalMetricFilter
import com.medihelp.app.feature_vitals.presentation.state.VitalTimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class VitalDashboardViewModel @Inject constructor(
    private val repository: VitalRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VitalDashboardUiState())
    val uiState: StateFlow<VitalDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeVitals().collect { records ->
                _uiState.update { state ->
                    state.copy(
                        records = records,
                        selectedCustomMetric = state.selectedCustomMetric
                            ?: records.firstOrNull { record ->
                                record.metricType.apiValue == "custom"
                            }?.metricName,
                        isLoading = false,
                    )
                }
            }
        }
        viewModelScope.launch { repository.refreshFromBackend() }
    }

    fun selectMetric(metric: VitalMetricFilter) {
        _uiState.update { it.copy(selectedMetric = metric) }
    }

    fun selectRange(range: VitalTimeRange) {
        _uiState.update { it.copy(selectedRange = range) }
    }

    fun selectCustomMetric(name: String) {
        _uiState.update { it.copy(selectedCustomMetric = name) }
    }
}
