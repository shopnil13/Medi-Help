package com.medihelp.app.feature_vitals.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_vitals.domain.model.BiomarkerDetail
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BiomarkerDetailUiState(
    val biomarker: BiomarkerDetail? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class BiomarkerDetailViewModel @Inject constructor(
    private val repository: VitalRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val biomarkerId: String = checkNotNull(savedStateHandle["biomarkerId"])
    private val _uiState = MutableStateFlow(BiomarkerDetailUiState())
    val uiState: StateFlow<BiomarkerDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiState.value = when (val result = repository.getBiomarkerDetail(biomarkerId)) {
                is Result.Success -> BiomarkerDetailUiState(
                    biomarker = result.data,
                    isLoading = false,
                )
                is Result.Error -> BiomarkerDetailUiState(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }
        }
    }
}
