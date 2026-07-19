package com.medihelp.app.feature_vitals.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.medihelp.app.MainDispatcherRule
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_vitals.domain.model.BiomarkerDetail
import com.medihelp.app.feature_vitals.domain.model.NewVitalInput
import com.medihelp.app.feature_vitals.domain.model.VitalRecord
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BiomarkerDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadsSimplifiedBiomarkerForNavigationId() = runTest {
        val repository = BiomarkerRepositoryFake()
        val viewModel = BiomarkerDetailViewModel(
            repository,
            SavedStateHandle(mapOf("biomarkerId" to "bio-1")),
        )

        advanceUntilIdle()

        assertEquals("bio-1", repository.requestedId)
        assertEquals("Fasting glucose", viewModel.uiState.value.biomarker?.name)
        assertTrue(viewModel.uiState.value.biomarker?.askDoctor == true)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}

private class BiomarkerRepositoryFake : VitalRepository {
    var requestedId: String? = null

    override fun observeVitals(): Flow<List<VitalRecord>> = emptyFlow()
    override suspend fun addVitals(inputs: List<NewVitalInput>): Result<Unit> = Result.Error("Unused")
    override suspend fun importConfirmedLab(jobId: String): Result<List<VitalRecord>> =
        Result.Error("Unused")
    override suspend fun refreshFromBackend() = Unit
    override suspend fun syncPendingChanges() = Unit

    override suspend fun getBiomarkerDetail(id: String): Result<BiomarkerDetail> {
        requestedId = id
        return Result.Success(
            BiomarkerDetail(
                id = id,
                name = "Fasting glucose",
                value = "110",
                unit = "mg/dL",
                referenceRange = "70-99",
                status = "high",
                recordedAt = Instant.EPOCH,
                explanation = "This measures blood sugar.",
                statusExplanation = "This is above the lab range.",
                moreDetails = "One result cannot explain why.",
                askDoctor = true,
            ),
        )
    }
}
