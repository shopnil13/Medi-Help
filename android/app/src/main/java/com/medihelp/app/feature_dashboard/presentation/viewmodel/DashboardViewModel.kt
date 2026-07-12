package com.medihelp.app.feature_dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.core.datastore.UserPreferencesDataStore
import com.medihelp.app.feature_auth.domain.repository.AuthRepository
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val displayName: String? = null,
    val activeMedicationCount: Int = 0,
    val hasLoggedOut: Boolean = false,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val medicationRepository: MedicationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesDataStore.displayName
                .combine(medicationRepository.observeActiveMedications()) { name, medications ->
                    name to medications.size
                }
                .collect { (name, count) ->
                    _uiState.update { it.copy(displayName = name, activeMedicationCount = count) }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(hasLoggedOut = true) }
        }
    }
}
