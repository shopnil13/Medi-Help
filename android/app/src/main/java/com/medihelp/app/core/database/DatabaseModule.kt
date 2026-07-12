package com.medihelp.app.core.database

import android.content.Context
import androidx.room.Room
import com.medihelp.app.core.database.dao.MedicationDao
import com.medihelp.app.core.database.dao.ReminderLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "medi_help.db").build()

    @Provides
    fun provideMedicationDao(database: AppDatabase): MedicationDao = database.medicationDao()

    @Provides
    fun provideReminderLogDao(database: AppDatabase): ReminderLogDao = database.reminderLogDao()
}
