package com.medihelp.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.medihelp.app.core.database.dao.MedicationDao
import com.medihelp.app.core.database.dao.DocumentDao
import com.medihelp.app.core.database.dao.ReminderLogDao
import com.medihelp.app.feature_medications.data.local.entity.MedicationEntity
import com.medihelp.app.feature_medications.data.local.entity.MedicationScheduleEntity
import com.medihelp.app.feature_medications.data.local.entity.ReminderLogEntity
import com.medihelp.app.feature_documents.data.local.entity.DocumentEntity

@Database(
    entities = [
        MedicationEntity::class,
        MedicationScheduleEntity::class,
        ReminderLogEntity::class,
        DocumentEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun reminderLogDao(): ReminderLogDao
    abstract fun documentDao(): DocumentDao
}
