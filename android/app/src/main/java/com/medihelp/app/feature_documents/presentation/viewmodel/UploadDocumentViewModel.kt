package com.medihelp.app.feature_documents.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_documents.domain.repository.DocumentRepository
import com.medihelp.app.feature_documents.presentation.state.SelectedDocument
import com.medihelp.app.feature_documents.presentation.state.UploadDocumentUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class UploadDocumentViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UploadDocumentUiState())
    val uiState: StateFlow<UploadDocumentUiState> = _uiState.asStateFlow()

    fun setDocumentType(value: String) {
        _uiState.update { it.copy(documentType = value, errorMessage = null) }
    }

    fun selectDocument(uri: Uri, filename: String, contentType: String) {
        _uiState.update {
            it.copy(
                selectedDocument = SelectedDocument(uri, filename, contentType),
                errorMessage = null,
            )
        }
    }

    fun upload() {
        val selected = _uiState.value.selectedDocument ?: return
        if (_uiState.value.isUploading) return
        _uiState.update { it.copy(isUploading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = documentRepository.uploadDocument(selected.uri, _uiState.value.documentType)) {
                is Result.Success -> _uiState.update {
                    it.copy(isUploading = false, uploadedJobId = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isUploading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun consumeUploadedJob() {
        _uiState.update { it.copy(uploadedJobId = null) }
    }
}
