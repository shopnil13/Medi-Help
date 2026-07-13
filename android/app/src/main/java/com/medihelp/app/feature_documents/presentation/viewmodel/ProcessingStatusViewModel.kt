package com.medihelp.app.feature_documents.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_documents.domain.repository.DocumentRepository
import com.medihelp.app.feature_documents.presentation.state.ProcessingStatusUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class ProcessingStatusViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentRepository: DocumentRepository,
) : ViewModel() {
    private val jobId: String = checkNotNull(savedStateHandle["jobId"])
    private val refreshError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProcessingStatusUiState> = combine(
        documentRepository.observeJob(jobId),
        refreshError,
    ) { document, error ->
        ProcessingStatusUiState(
            document = document,
            isLoading = document == null,
            refreshError = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProcessingStatusUiState(),
    )

    init {
        viewModelScope.launch {
            while (isActive) {
                refreshOnce()
                if (uiState.value.document?.jobStatus in TERMINAL_STATUSES) break
                delay(POLL_INTERVAL_MILLIS)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshOnce()
        }
    }

    private suspend fun refreshOnce() {
        when (val result = documentRepository.refreshJob(jobId)) {
            is Result.Success -> refreshError.value = null
            is Result.Error -> refreshError.value = result.message
        }
    }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 3_000L
        val TERMINAL_STATUSES = setOf("needs_review", "completed", "failed")
    }
}
