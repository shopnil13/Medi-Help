package com.medihelp.app.feature_medications.data.repository

import androidx.room.withTransaction
import com.medihelp.app.core.common.Result
import com.medihelp.app.core.common.toUserMessage
import com.medihelp.app.core.database.AppDatabase
import com.medihelp.app.core.database.dao.MedicationDao
import com.medihelp.app.core.database.dao.ReminderLogDao
import com.medihelp.app.core.reminders.MedicationAlarmScheduler
import com.medihelp.app.feature_medications.data.local.entity.MedicationEntity
import com.medihelp.app.feature_medications.data.local.entity.MedicationScheduleEntity
import com.medihelp.app.feature_medications.data.local.entity.ReminderLogEntity
import com.medihelp.app.feature_medications.data.mapper.newLocalId
import com.medihelp.app.feature_medications.data.mapper.toCreateRequestDto
import com.medihelp.app.feature_medications.data.mapper.toDomain
import com.medihelp.app.feature_medications.data.mapper.toEntity
import com.medihelp.app.feature_medications.data.remote.MedicationApi
import com.medihelp.app.feature_medications.data.remote.dto.MedicationResponseDto
import com.medihelp.app.feature_medications.data.remote.dto.MedicationUpdateRequestDto
import com.medihelp.app.feature_medications.data.remote.dto.ReminderLogRequestDto
import com.medihelp.app.feature_medications.data.remote.dto.ConfirmExtractedMedicationsRequestDto
import com.medihelp.app.feature_medications.domain.model.Medication
import com.medihelp.app.feature_medications.domain.model.MedicationStatus
import com.medihelp.app.feature_medications.domain.model.NewMedicationInput
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

private const val LOCAL_ID_PREFIX = "local-"

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationApi: MedicationApi,
    private val medicationDao: MedicationDao,
    private val reminderLogDao: ReminderLogDao,
    private val alarmScheduler: MedicationAlarmScheduler,
    private val appDatabase: AppDatabase,
) : MedicationRepository {

    override fun observeActiveMedications(): Flow<List<Medication>> =
        medicationDao.observeActiveMedications().map { list -> list.map { it.toDomain() } }

    override fun observeMedication(id: String): Flow<Medication?> =
        medicationDao.observeMedication(id).map { it?.toDomain() }

    override suspend fun addMedication(input: NewMedicationInput): Result<Unit> {
        val tempId = LOCAL_ID_PREFIX + newLocalId()
        val medicationEntity = buildDraftEntity(tempId, input)
        val scheduleEntities = buildDraftSchedules(tempId, input)

        appDatabase.withTransaction {
            medicationDao.insertMedication(medicationEntity)
            medicationDao.insertSchedules(scheduleEntities)
        }
        alarmScheduler.scheduleAllForMedication(tempId, medicationEntity.name, scheduleEntities)

        return try {
            val response = medicationApi.createMedication(input.toCreateRequestDto())
            replaceWithSyncedCopy(tempId, scheduleEntities.map { it.id }, response)
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(e.toUserMessage())
        } catch (e: IOException) {
            // Offline: the medicine is already saved and reminders are already scheduled
            // locally. syncPendingChanges() will pick it up next time it runs.
            Result.Success(Unit)
        }
    }

    private fun buildDraftEntity(id: String, input: NewMedicationInput): MedicationEntity {
        val now = System.currentTimeMillis()
        return MedicationEntity(
            id = id,
            serverId = null,
            name = input.name.trim(),
            strength = input.strength,
            dosageInstruction = input.dosageInstruction.trim(),
            simplifiedInstruction = null,
            purposeSimplified = null,
            startDateEpochDay = input.startDate?.toEpochDay(),
            endDateEpochDay = input.endDate?.toEpochDay(),
            status = "active",
            requiresReview = false,
            isSynced = false,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
        )
    }

    private fun buildDraftSchedules(
        medicationId: String,
        input: NewMedicationInput,
    ): List<MedicationScheduleEntity> = input.schedules.map { schedule ->
        MedicationScheduleEntity(
            id = newLocalId(),
            serverId = null,
            medicationId = medicationId,
            timeOfDayMinutes = schedule.timeOfDay.toSecondOfDay() / 60,
            frequencyType = "daily",
            daysOfWeek = null,
            mealRelation = schedule.mealRelation,
            doseAmount = schedule.doseAmount,
            notes = null,
        )
    }

    /**
     * Re-keys a medication from its temporary local id to the server-assigned id.
     * Reminder logs are repointed to the new id *before* the old row is deleted,
     * since [ReminderLogEntity] has a cascading foreign key on medicationId —
     * deleting first would silently wipe any logs recorded while offline.
     */
    private suspend fun replaceWithSyncedCopy(
        tempId: String,
        tempScheduleIds: List<String>,
        response: MedicationResponseDto,
    ) {
        val serverId = response.id
        val syncedEntity = response.toEntity(localId = serverId, isSynced = true)
        val syncedSchedules = response.schedules.map { it.toEntity(localId = it.id, medicationLocalId = serverId) }

        appDatabase.withTransaction {
            reminderLogDao.repointMedicationId(tempId, serverId)
            medicationDao.deleteMedication(tempId)
            medicationDao.insertMedication(syncedEntity)
            medicationDao.insertSchedules(syncedSchedules)
        }

        alarmScheduler.cancelAll(tempScheduleIds)
        alarmScheduler.scheduleAllForMedication(serverId, syncedEntity.name, syncedSchedules)
    }

    override suspend fun updateStatus(id: String, status: MedicationStatus): Result<Unit> {
        val medication = medicationDao.getMedicationOnce(id)
        val apiValue = status.name.lowercase()
        medicationDao.updateStatus(id, apiValue, System.currentTimeMillis())
        medication?.let { cached ->
            if (status == MedicationStatus.ACTIVE) {
                alarmScheduler.scheduleAllForMedication(
                    cached.medication.id,
                    cached.medication.name,
                    cached.schedules,
                )
            } else {
                alarmScheduler.cancelAll(cached.schedules.map { it.id })
            }
        }

        if (id.startsWith(LOCAL_ID_PREFIX)) {
            return Result.Success(Unit)
        }

        return try {
            medicationApi.updateMedication(id, MedicationUpdateRequestDto(status = apiValue))
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(e.toUserMessage())
        } catch (e: IOException) {
            Result.Success(Unit)
        }
    }

    override suspend fun deleteMedication(id: String): Result<Unit> {
        alarmScheduler.cancelReminder(id)
        medicationDao.deleteMedication(id)

        if (id.startsWith(LOCAL_ID_PREFIX)) {
            return Result.Success(Unit)
        }

        return try {
            medicationApi.deleteMedication(id)
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(e.toUserMessage())
        } catch (e: IOException) {
            Result.Success(Unit)
        }
    }

    override suspend fun logReminderAction(medicationId: String, scheduledAt: Instant, action: String) {
        val actionAt = System.currentTimeMillis()
        val logEntity = ReminderLogEntity(
            id = newLocalId(),
            medicationId = medicationId,
            scheduledAtEpochMillis = scheduledAt.toEpochMilli(),
            action = action,
            actionAtEpochMillis = actionAt,
            isSynced = false,
        )
        reminderLogDao.insert(logEntity)

        if (medicationId.startsWith(LOCAL_ID_PREFIX)) return

        try {
            medicationApi.logReminder(
                ReminderLogRequestDto(
                    medicationId = medicationId,
                    scheduledAt = DateTimeFormatter.ISO_INSTANT.format(scheduledAt),
                    action = action,
                    actionAt = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(actionAt)),
                ),
            )
            reminderLogDao.markSynced(logEntity.id)
        } catch (_: Exception) {
            // Left unsynced; syncPendingChanges() can retry later.
        }
    }

    override suspend fun refreshFromBackend() {
        try {
            val remoteMedications = medicationApi.getMedications()
            remoteMedications.forEach { dto -> cacheRemoteMedication(dto) }
        } catch (_: Exception) {
            // Best-effort refresh; local cache stays as the source of truth on failure.
        }
    }

    override suspend fun syncPendingChanges() {
        medicationDao.getUnsyncedMedications().forEach { unsynced ->
            val input = NewMedicationInput(
                name = unsynced.medication.name,
                strength = unsynced.medication.strength,
                dosageInstruction = unsynced.medication.dosageInstruction,
                startDate = null,
                endDate = null,
                schedules = emptyList(),
            )
            try {
                val response = medicationApi.createMedication(input.toCreateRequestDto())
                replaceWithSyncedCopy(
                    unsynced.medication.id,
                    unsynced.schedules.map { it.id },
                    response,
                )
            } catch (_: Exception) {
                // Still offline or backend rejected it; try again on the next sync pass.
            }
        }

        reminderLogDao.getUnsyncedLogs().forEach { log ->
            if (log.medicationId.startsWith(LOCAL_ID_PREFIX)) return@forEach
            try {
                medicationApi.logReminder(
                    ReminderLogRequestDto(
                        medicationId = log.medicationId,
                        scheduledAt = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(log.scheduledAtEpochMillis)),
                        action = log.action,
                        actionAt = log.actionAtEpochMillis?.let {
                            DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(it))
                        },
                    ),
                )
                reminderLogDao.markSynced(log.id)
            } catch (_: Exception) {
                // Try again on the next sync pass.
            }
        }
    }

    private suspend fun cacheRemoteMedication(dto: MedicationResponseDto) {
        val entity = dto.toEntity(localId = dto.id, isSynced = true)
        val schedules = dto.schedules.map { it.toEntity(localId = it.id, medicationLocalId = dto.id) }

        appDatabase.withTransaction {
            medicationDao.insertMedication(entity)
            medicationDao.deleteSchedulesForMedication(dto.id)
            medicationDao.insertSchedules(schedules)
        }

        if (entity.status == "active") {
            alarmScheduler.scheduleAllForMedication(dto.id, entity.name, schedules)
        } else {
            alarmScheduler.cancelAll(schedules.map { it.id })
        }
    }

    override suspend fun importConfirmedPrescription(jobId: String): Result<List<Medication>> {
        return try {
            val responses = medicationApi.confirmExtractedMedications(
                ConfirmExtractedMedicationsRequestDto(jobId),
            )
            responses.forEach { cacheRemoteMedication(it) }
            Result.Success(responses.map { it.toDomain() })
        } catch (error: Exception) {
            Result.Error(error.toUserMessage())
        }
    }

    override suspend fun simplifyMedication(id: String): Result<Unit> {
        if (id.startsWith(LOCAL_ID_PREFIX)) return Result.Success(Unit)
        return try {
            cacheRemoteMedication(medicationApi.simplifyMedication(id))
            Result.Success(Unit)
        } catch (error: Exception) {
            Result.Error(error.toUserMessage())
        }
    }
}
