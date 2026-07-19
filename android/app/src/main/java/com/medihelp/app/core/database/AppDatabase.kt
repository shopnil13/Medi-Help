package com.medihelp.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.medihelp.app.core.database.dao.MedicationDao
import com.medihelp.app.core.database.dao.DocumentDao
import com.medihelp.app.core.database.dao.ReminderLogDao
import com.medihelp.app.core.database.dao.VitalDao
import com.medihelp.app.feature_medications.data.local.entity.MedicationEntity
import com.medihelp.app.feature_medications.data.local.entity.MedicationScheduleEntity
import com.medihelp.app.feature_medications.data.local.entity.ReminderLogEntity
import com.medihelp.app.feature_documents.data.local.entity.DocumentEntity
import com.medihelp.app.feature_vitals.data.local.entity.VitalRecordEntity

@Database(
    entities = [
        MedicationEntity::class,
        MedicationScheduleEntity::class,
        ReminderLogEntity::class,
        DocumentEntity::class,
        VitalRecordEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun reminderLogDao(): ReminderLogDao
    abstract fun documentDao(): DocumentDao
    abstract fun vitalDao(): VitalDao
}
