package com.medihelp.app.core.database.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medihelp.app.feature_medications.data.local.entity.ReminderLogEntity
import kotlinx.coroutines.flow.Flow

@androidx.room.Dao
interface ReminderLogDao {

    @Query("SELECT * FROM reminder_logs ORDER BY scheduledAtEpochMillis DESC")
    fun observeReminderLogs(): Flow<List<ReminderLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ReminderLogEntity)

    @Query("SELECT * FROM reminder_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<ReminderLogEntity>

    @Query("UPDATE reminder_logs SET isSynced = 1 WHERE id = :logId")
    suspend fun markSynced(logId: String)

    @Query("UPDATE reminder_logs SET medicationId = :newMedicationId WHERE medicationId = :oldMedicationId")
    suspend fun repointMedicationId(oldMedicationId: String, newMedicationId: String)
}
