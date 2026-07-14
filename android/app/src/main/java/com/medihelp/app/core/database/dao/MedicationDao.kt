package com.medihelp.app.core.database.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.medihelp.app.feature_medications.data.local.entity.MedicationEntity
import com.medihelp.app.feature_medications.data.local.entity.MedicationScheduleEntity
import com.medihelp.app.feature_medications.data.local.entity.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow

@androidx.room.Dao
interface MedicationDao {

    @Transaction
    @Query("SELECT * FROM medications WHERE status = 'active' ORDER BY createdAtEpochMillis DESC")
    fun observeActiveMedications(): Flow<List<MedicationWithSchedules>>

    @Transaction
    @Query("SELECT * FROM medications WHERE status = 'active'")
    suspend fun getActiveMedicationsOnce(): List<MedicationWithSchedules>

    @Transaction
    @Query("SELECT * FROM medications ORDER BY createdAtEpochMillis DESC")
    fun observeAllMedications(): Flow<List<MedicationWithSchedules>>

    @Transaction
    @Query("SELECT * FROM medications WHERE id = :medicationId")
    fun observeMedication(medicationId: String): Flow<MedicationWithSchedules?>

    @Transaction
    @Query("SELECT * FROM medications WHERE id = :medicationId LIMIT 1")
    suspend fun getMedicationOnce(medicationId: String): MedicationWithSchedules?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<MedicationScheduleEntity>)

    @Query("DELETE FROM medications WHERE id = :medicationId")
    suspend fun deleteMedication(medicationId: String)

    @Query("DELETE FROM medication_schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: String)

    @Transaction
    @Query("SELECT * FROM medications WHERE isSynced = 0")
    suspend fun getUnsyncedMedications(): List<MedicationWithSchedules>

    @Query(
        "UPDATE medications SET status = :status, updatedAtEpochMillis = :updatedAt " +
            "WHERE id = :medicationId",
    )
    suspend fun updateStatus(medicationId: String, status: String, updatedAt: Long)
}
