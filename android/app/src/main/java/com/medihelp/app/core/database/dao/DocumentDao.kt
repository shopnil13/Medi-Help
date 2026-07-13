package com.medihelp.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medihelp.app.feature_documents.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE jobId = :jobId LIMIT 1")
    fun observeByJobId(jobId: String): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(document: DocumentEntity)
}
