package com.medihelp.app.feature_documents.presentation.screen

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.medihelp.app.core.designsystem.components.MediHelpPrimaryButton
import com.medihelp.app.core.designsystem.components.MediHelpTopBar
import com.medihelp.app.core.designsystem.theme.MediHelpSpacing
import com.medihelp.app.feature_documents.presentation.viewmodel.UploadDocumentViewModel

private data class DocumentTypeOption(val label: String, val value: String)

private val documentTypes = listOf(
    DocumentTypeOption("Prescription", "prescription"),
    DocumentTypeOption("Lab report", "lab_report"),
    DocumentTypeOption("Not sure", "unknown"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDocumentScreen(
    onOpenCamera: () -> Unit,
    onUploadStarted: (String) -> Unit,
    capturedUri: Uri? = null,
    onCapturedUriConsumed: () -> Unit = {},
    viewModel: UploadDocumentViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    fun selectUri(uri: Uri) {
        val resolver = context.contentResolver
        val filename = resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: "document"
        val contentType = resolver.getType(uri) ?: "image/jpeg"
        viewModel.selectDocument(uri, filename, contentType)
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            selectUri(it)
        }
    }

    LaunchedEffect(capturedUri) {
        capturedUri?.let {
            selectUri(it)
            onCapturedUriConsumed()
        }
    }

    LaunchedEffect(uiState.uploadedJobId) {
        uiState.uploadedJobId?.let { jobId ->
            viewModel.consumeUploadedJob()
            onUploadStarted(jobId)
        }
    }

    Scaffold(
        topBar = { MediHelpTopBar(title = "Upload document") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(MediHelpSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space4),
        ) {
            Text("Document type", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                documentTypes.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = uiState.documentType == option.value,
                        onClick = { viewModel.setDocumentType(option.value) },
                        shape = SegmentedButtonDefaults.itemShape(index, documentTypes.size),
                    ) {
                        Text(option.label)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TextButton(
                    onClick = { filePicker.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) },
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null)
                    Text("Choose file", modifier = Modifier.padding(start = MediHelpSpacing.space2))
                }
                TextButton(onClick = onOpenCamera) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Text("Camera", modifier = Modifier.padding(start = MediHelpSpacing.space2))
                }
            }

            uiState.selectedDocument?.let { selected ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(MediHelpSpacing.space4),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(MediHelpSpacing.space3),
                    ) {
                        if (selected.contentType.startsWith("image/")) {
                            AsyncImage(
                                model = selected.uri,
                                contentDescription = "Selected document preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f),
                                contentScale = ContentScale.Fit,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(selected.filename, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            MediHelpPrimaryButton(
                text = "Upload",
                onClick = viewModel::upload,
                enabled = uiState.selectedDocument != null,
                isLoading = uiState.isUploading,
            )
        }
    }
}
