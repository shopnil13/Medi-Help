package com.medihelp.app.feature_vitals.presentation.viewmodel

import com.medihelp.app.MainDispatcherRule
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_vitals.domain.model.NewVitalInput
import com.medihelp.app.feature_vitals.domain.model.VitalMetricType
import com.medihelp.app.feature_vitals.domain.model.VitalRecord
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import com.medihelp.app.feature_vitals.presentation.state.AddVitalKind
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
class AddVitalViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun bloodPressureSaveCreatesPairedRecords() = runTest {
        val repository = FakeVitalRepository()
        val viewModel = AddVitalViewModel(repository)
        viewModel.onPrimaryValueChange("120")
        viewModel.onSecondaryValueChange("80")

        viewModel.save()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveSucceeded)
        assertEquals(2, repository.saved.size)
        assertEquals(VitalMetricType.BLOOD_PRESSURE_SYSTOLIC, repository.saved[0].metricType)
        assertEquals(VitalMetricType.BLOOD_PRESSURE_DIASTOLIC, repository.saved[1].metricType)
        assertEquals(repository.saved[0].recordedAt, repository.saved[1].recordedAt)
    }

    @Test
    fun customMetricRequiresNameAndUnit() = runTest {
        val repository = FakeVitalRepository()
        val viewModel = AddVitalViewModel(repository)
        viewModel.selectKind(AddVitalKind.CUSTOM)
        viewModel.onPrimaryValueChange("37.2")

        viewModel.save()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.saveSucceeded)
        assertEquals("Enter a marker name.", viewModel.uiState.value.errorMessage)
        assertTrue(repository.saved.isEmpty())
    }
}

private class FakeVitalRepository : VitalRepository {
    var saved: List<NewVitalInput> = emptyList()

    override fun observeVitals(): Flow<List<VitalRecord>> = emptyFlow()

    override suspend fun addVitals(inputs: List<NewVitalInput>): Result<Unit> {
        saved = inputs
        return Result.Success(Unit)
    }

    override suspend fun importConfirmedLab(jobId: String): Result<List<VitalRecord>> =
        Result.Error("Not used")

    override suspend fun refreshFromBackend() = Unit

    override suspend fun syncPendingChanges() = Unit

    override suspend fun getBiomarkerDetail(id: String) = Result.Error("Not used")
}
