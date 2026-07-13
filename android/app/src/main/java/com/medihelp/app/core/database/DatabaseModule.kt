package com.medihelp.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medihelp.app.core.database.dao.DocumentDao
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

    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS documents (
                    id TEXT NOT NULL PRIMARY KEY,
                    jobId TEXT NOT NULL,
                    documentType TEXT NOT NULL,
                    originalFilename TEXT NOT NULL,
                    contentType TEXT NOT NULL,
                    fileSizeBytes INTEGER NOT NULL,
                    documentStatus TEXT NOT NULL,
                    jobStatus TEXT NOT NULL,
                    progressPercent INTEGER NOT NULL,
                    errorMessage TEXT,
                    createdAtEpochMillis INTEGER NOT NULL,
                    updatedAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE documents ADD COLUMN structuredResultJson TEXT")
            db.execSQL("ALTER TABLE documents ADD COLUMN confirmedResultJson TEXT")
            db.execSQL("ALTER TABLE documents ADD COLUMN confirmedAtEpochMillis INTEGER")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "medi_help.db")
            .addMigrations(migration1To2, migration2To3)
            .build()

    @Provides
    fun provideMedicationDao(database: AppDatabase): MedicationDao = database.medicationDao()

    @Provides
    fun provideReminderLogDao(database: AppDatabase): ReminderLogDao = database.reminderLogDao()

    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao = database.documentDao()
}
