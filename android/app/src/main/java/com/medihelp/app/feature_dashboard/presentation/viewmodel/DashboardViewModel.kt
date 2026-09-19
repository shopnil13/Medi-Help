package com.medihelp.app.feature_dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.core.datastore.UserPreferencesDataStore
import com.medihelp.app.feature_auth.domain.repository.AuthRepository
import com.medihelp.app.feature_dashboard.domain.DayGreeting
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import com.medihelp.app.feature_medications.domain.model.dosesDueOn
import com.medihelp.app.feature_vitals.domain.model.VitalsSummary
import com.medihelp.app.feature_vitals.domain.model.toDashboardSummary
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val displayName: String? = null,
    val greeting: DayGreeting = DayGreeting.MORNING,
    val dosesDueToday: Int = 0,
    val vitalsSummary: VitalsSummary = VitalsSummary(),
    val hasLoggedOut: Boolean = false,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val medicationRepository: MedicationRepository,
    private val vitalRepository: VitalRepository,
    // Injected so the greeting and "due today" boundaries are testable without
    // waiting for a real clock to roll over.
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // The dashboard is the landing screen after login, so it is the first
        // place stale counts would show. Both calls read through to the local
        // cache and swallow network failures, keeping the screen offline-safe.
        viewModelScope.launch { medicationRepository.refreshFromBackend() }
        viewModelScope.launch { vitalRepository.refreshFromBackend() }

        viewModelScope.launch {
            combine(
                userPreferencesDataStore.displayName,
                medicationRepository.observeActiveMedications(),
                vitalRepository.observeVitals(),
            ) { name, medications, vitals ->
                val now = LocalDate.now(clock)
                DashboardUiState(
                    displayName = name,
                    greeting = DayGreeting.forTime(LocalTime.now(clock)),
                    dosesDueToday = medications.dosesDueOn(now),
                    vitalsSummary = vitals.toDashboardSummary(),
                )
            }.collect { state ->
                _uiState.update {
                    state.copy(hasLoggedOut = it.hasLoggedOut)
                }
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
