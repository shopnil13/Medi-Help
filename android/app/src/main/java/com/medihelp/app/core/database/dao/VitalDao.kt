package com.medihelp.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medihelp.app.feature_vitals.data.local.entity.VitalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalDao {
    @Query("SELECT * FROM vital_records ORDER BY recordedAtEpochMillis DESC")
    fun observeAll(): Flow<List<VitalRecordEntity>>

    @Query("SELECT * FROM vital_records WHERE isSynced = 0 ORDER BY recordedAtEpochMillis ASC")
    suspend fun getUnsynced(): List<VitalRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<VitalRecordEntity>)

    @Query("DELETE FROM vital_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}

