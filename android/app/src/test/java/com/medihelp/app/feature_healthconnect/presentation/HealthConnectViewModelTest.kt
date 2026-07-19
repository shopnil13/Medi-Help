package com.medihelp.app.feature_healthconnect.presentation

import com.medihelp.app.MainDispatcherRule
import com.medihelp.app.core.common.Result
import com.medihelp.app.feature_healthconnect.domain.model.HealthConnectAvailability
import com.medihelp.app.feature_healthconnect.domain.repository.HealthConnectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HealthConnectViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun grantingPermissionsEnablesSyncAndImportsRecords() = runTest {
        val repository = FakeHealthConnectRepository(hasPermissions = false, importedCount = 4)
        val viewModel = HealthConnectViewModel(repository)
        advanceUntilIdle()

        viewModel.setSyncEnabled(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.shouldRequestPermissions)

        repository.hasPermissions = true
        viewModel.onPermissionResult(granted = true)
        advanceUntilIdle()

        assertTrue(repository.syncEnabled.value)
        assertEquals(1, repository.importCalls)
        assertEquals(4, viewModel.uiState.value.lastImportedCount)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun refreshDisablesSyncWhenPermissionsWereRevoked() = runTest {
        val repository = FakeHealthConnectRepository(hasPermissions = true, importedCount = 0)
        repository.syncEnabled.value = true
        val viewModel = HealthConnectViewModel(repository)
        advanceUntilIdle()

        repository.hasPermissions = false
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(repository.syncEnabled.value)
        assertFalse(viewModel.uiState.value.hasPermissions)
        assertFalse(viewModel.uiState.value.syncEnabled)
    }
}

private class FakeHealthConnectRepository(
    var hasPermissions: Boolean,
    private val importedCount: Int,
) : HealthConnectRepository {
    override val requiredPermissions: Set<String> = setOf("heart_rate", "blood_pressure")
    val syncEnabled = MutableStateFlow(false)
    var importCalls = 0

    override fun observeSyncEnabled(): Flow<Boolean> = syncEnabled

    override fun availability(): HealthConnectAvailability = HealthConnectAvailability.AVAILABLE

    override suspend fun hasAllPermissions(): Boolean = hasPermissions

    override suspend fun setSyncEnabled(enabled: Boolean) {
        syncEnabled.value = enabled
    }

    override suspend fun importRecentRecords(): Result<Int> {
        importCalls += 1
        return Result.Success(importedCount)
    }
}
