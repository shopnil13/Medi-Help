package com.medihelp.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.medihelp.app.core.database.dao.MedicationDao
import com.medihelp.app.core.database.dao.ReminderLogDao
import com.medihelp.app.feature_medications.data.local.entity.MedicationEntity
import com.medihelp.app.feature_medications.data.local.entity.MedicationScheduleEntity
import com.medihelp.app.feature_medications.data.local.entity.ReminderLogEntity

@Database(
    entities = [
        MedicationEntity::class,
        MedicationScheduleEntity::class,
        ReminderLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun reminderLogDao(): ReminderLogDao
}
