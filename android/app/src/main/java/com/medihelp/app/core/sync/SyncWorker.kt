package com.medihelp.app.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val medicationRepository: MedicationRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        medicationRepository.syncPendingChanges()
        medicationRepository.refreshFromBackend()
        return Result.success()
    }
}
