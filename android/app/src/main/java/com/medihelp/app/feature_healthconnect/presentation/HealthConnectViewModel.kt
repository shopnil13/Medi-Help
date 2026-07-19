package com.medihelp.app.feature_healthconnect.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_healthconnect.domain.model.HealthConnectAvailability
import com.medihelp.app.feature_healthconnect.domain.repository.HealthConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val repository: HealthConnectRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HealthConnectUiState())
    val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()
    val requiredPermissions: Set<String> = repository.requiredPermissions

    init {
        viewModelScope.launch {
            repository.observeSyncEnabled().collect { enabled ->
                _uiState.update { it.copy(syncEnabled = enabled) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val availability = repository.availability()
            val permissions = availability == HealthConnectAvailability.AVAILABLE &&
                repository.hasAllPermissions()
            if (!permissions && _uiState.value.syncEnabled) {
                repository.setSyncEnabled(false)
            }
            _uiState.update {
                it.copy(
                    availability = availability,
                    hasPermissions = permissions,
                    isChecking = false,
                    errorMessage = null,
                )
            }
        }
    }

    fun setSyncEnabled(enabled: Boolean) {
        if (!enabled) {
            viewModelScope.launch {
                repository.setSyncEnabled(false)
                _uiState.update { it.copy(errorMessage = null) }
            }
            return
        }
        viewModelScope.launch {
            if (repository.availability() != HealthConnectAvailability.AVAILABLE) {
                _uiState.update { it.copy(errorMessage = "Health Connect is not available.") }
            } else if (!repository.hasAllPermissions()) {
                _uiState.update { it.copy(shouldRequestPermissions = true, errorMessage = null) }
            } else {
                enableAndSync()
            }
        }
    }

    fun onPermissionRequestLaunched() {
        _uiState.update { it.copy(shouldRequestPermissions = false) }
    }

    fun onPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (granted) {
                _uiState.update { it.copy(hasPermissions = true, errorMessage = null) }
                enableAndSync()
            } else {
                repository.setSyncEnabled(false)
                _uiState.update {
                    it.copy(
                        hasPermissions = false,
                        errorMessage = "Permission was not granted. You can manage access in Health Connect.",
                    )
                }
            }
        }
    }

    fun syncNow() {
        if (_uiState.value.isSyncing) return
        viewModelScope.launch { importRecords() }
    }

    private suspend fun enableAndSync() {
        repository.setSyncEnabled(true)
        importRecords()
    }

    private suspend fun importRecords() {
        _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
        when (val result = repository.importRecentRecords()) {
            is Result.Success -> _uiState.update {
                it.copy(isSyncing = false, lastImportedCount = result.data)
            }
            is Result.Error -> _uiState.update {
                it.copy(isSyncing = false, errorMessage = result.message)
            }
        }
    }
}
