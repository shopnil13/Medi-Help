package com.medihelp.app.feature_vitals.data.repository

import androidx.room.withTransaction
import com.medihelp.app.core.common.Result
import com.medihelp.app.core.common.toUserMessage
import com.medihelp.app.core.database.AppDatabase
import com.medihelp.app.core.database.dao.VitalDao
import com.medihelp.app.feature_vitals.data.mapper.toCreateRequest
import com.medihelp.app.feature_vitals.data.mapper.toDomain
import com.medihelp.app.feature_vitals.data.mapper.toEntity
import com.medihelp.app.feature_vitals.data.mapper.toLocalEntity
import com.medihelp.app.feature_vitals.data.local.entity.VitalRecordEntity
import com.medihelp.app.feature_vitals.data.remote.VitalApi
import com.medihelp.app.feature_vitals.data.remote.dto.VitalBulkCreateRequestDto
import com.medihelp.app.feature_vitals.data.remote.dto.ConfirmExtractedLabRequestDto
import com.medihelp.app.feature_vitals.domain.model.NewVitalInput
import com.medihelp.app.feature_vitals.domain.model.VitalRecord
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

@Singleton
class VitalRepositoryImpl @Inject constructor(
    private val api: VitalApi,
    private val dao: VitalDao,
    private val database: AppDatabase,
) : VitalRepository {
    override fun observeVitals(): Flow<List<VitalRecord>> =
        dao.observeAll().map { records -> records.map { it.toDomain() } }

    override suspend fun addVitals(inputs: List<NewVitalInput>): Result<Unit> {
        val localRecords = inputs.map { it.toLocalEntity() }
        dao.insertAll(localRecords)
        return try {
            val response = api.bulkSync(
                VitalBulkCreateRequestDto(localRecords.map { it.toCreateRequest() }),
            )
            replaceLocalRecords(localRecords.map { it.id }, response.map { it.toEntity() })
            Result.Success(Unit)
        } catch (error: IOException) {
            Result.Success(Unit)
        } catch (error: HttpException) {
            if (error.code() >= 500) {
                Result.Success(Unit)
            } else {
                dao.deleteByIds(localRecords.map { it.id })
                Result.Error(error.toUserMessage())
            }
        }
    }

    override suspend fun importConfirmedLab(jobId: String): Result<List<VitalRecord>> {
        return try {
            val response = api.confirmExtractedLab(ConfirmExtractedLabRequestDto(jobId))
            val records = response.vitalRecords.map { it.toEntity() }
            dao.insertAll(records)
            Result.Success(records.map { it.toDomain() })
        } catch (error: Exception) {
            Result.Error(error.toUserMessage())
        }
    }

    override suspend fun refreshFromBackend() {
        runCatching { api.getVitals() }
            .onSuccess { response -> dao.insertAll(response.map { it.toEntity() }) }
    }

    override suspend fun syncPendingChanges() {
        val pending = dao.getUnsynced()
        if (pending.isEmpty()) return
        runCatching {
            api.bulkSync(VitalBulkCreateRequestDto(pending.map { it.toCreateRequest() }))
        }.onSuccess { response ->
            replaceLocalRecords(pending.map { it.id }, response.map { it.toEntity() })
        }
    }

    private suspend fun replaceLocalRecords(
        localIds: List<String>,
        syncedRecords: List<VitalRecordEntity>,
    ) {
        database.withTransaction {
            dao.deleteByIds(localIds)
            dao.insertAll(syncedRecords)
        }
    }
}
