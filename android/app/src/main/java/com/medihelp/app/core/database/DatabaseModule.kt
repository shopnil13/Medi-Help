package com.medihelp.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medihelp.app.core.database.dao.DocumentDao
import com.medihelp.app.core.database.dao.MedicationDao
import com.medihelp.app.core.database.dao.ReminderLogDao
import com.medihelp.app.core.database.dao.VitalDao
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

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS vital_records (
                    id TEXT NOT NULL PRIMARY KEY,
                    serverId TEXT,
                    metricType TEXT NOT NULL,
                    metricName TEXT NOT NULL,
                    valueNumeric REAL NOT NULL,
                    unit TEXT NOT NULL,
                    recordedAtEpochMillis INTEGER NOT NULL,
                    source TEXT NOT NULL,
                    sourceDocumentId TEXT,
                    notes TEXT,
                    isSynced INTEGER NOT NULL,
                    createdAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_vital_records_metricType ON vital_records(metricType)")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_vital_records_recordedAtEpochMillis " +
                    "ON vital_records(recordedAtEpochMillis)",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_vital_records_source ON vital_records(source)")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_vital_records_serverId ON vital_records(serverId)",
            )
        }
    }

    private val migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE vital_records ADD COLUMN sourceJobId TEXT")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_vital_records_sourceJobId ON vital_records(sourceJobId)",
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "medi_help.db")
            .addMigrations(migration1To2, migration2To3, migration3To4, migration4To5)
            .build()

    @Provides
    fun provideMedicationDao(database: AppDatabase): MedicationDao = database.medicationDao()

    @Provides
    fun provideReminderLogDao(database: AppDatabase): ReminderLogDao = database.reminderLogDao()

    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao = database.documentDao()

    @Provides
    fun provideVitalDao(database: AppDatabase): VitalDao = database.vitalDao()
}
