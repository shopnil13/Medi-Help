package com.medihelp.app.feature_healthconnect.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.medihelp.app.core.common.Result
import com.medihelp.app.core.common.toUserMessage
import com.medihelp.app.core.datastore.UserPreferencesDataStore
import com.medihelp.app.feature_healthconnect.domain.model.HealthConnectAvailability
import com.medihelp.app.feature_healthconnect.domain.repository.HealthConnectRepository
import com.medihelp.app.feature_vitals.domain.model.NewVitalInput
import com.medihelp.app.feature_vitals.domain.model.VitalMetricType
import com.medihelp.app.feature_vitals.domain.model.VitalSource
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class HealthConnectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataStore,
    private val vitalRepository: VitalRepository,
) : HealthConnectRepository {
    override val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
    )

    override fun observeSyncEnabled(): Flow<Boolean> = preferences.healthConnectSyncEnabled

    override fun availability(): HealthConnectAvailability = when (
        HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE_NAME)
    ) {
        HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
            HealthConnectAvailability.UPDATE_REQUIRED
        else -> HealthConnectAvailability.UNAVAILABLE
    }

    override suspend fun hasAllPermissions(): Boolean {
        if (availability() != HealthConnectAvailability.AVAILABLE) return false
        val granted = client().permissionController.getGrantedPermissions()
        return granted.containsAll(requiredPermissions)
    }

    override suspend fun setSyncEnabled(enabled: Boolean) {
        preferences.setHealthConnectSyncEnabled(enabled)
    }

    override suspend fun importRecentRecords(): Result<Int> {
        if (!preferences.healthConnectSyncEnabled.first()) {
            return Result.Error("Health Connect sync is turned off.")
        }
        if (!hasAllPermissions()) {
            return Result.Error("Health Connect permissions are required.")
        }
        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(30, ChronoUnit.DAYS)
            val healthClient = client()
            val inputs = buildList {
                readAll<HeartRateRecord>(healthClient, startTime, endTime).forEach { record ->
                    record.samples.forEach { sample ->
                        add(
                            wearableInput(
                                VitalMetricType.HEART_RATE,
                                sample.beatsPerMinute.toDouble(),
                                "bpm",
                                sample.time,
                            ),
                        )
                    }
                }
                readAll<BloodPressureRecord>(healthClient, startTime, endTime).forEach { record ->
                    add(
                        wearableInput(
                            VitalMetricType.BLOOD_PRESSURE_SYSTOLIC,
                            record.systolic.inMillimetersOfMercury,
                            "mmHg",
                            record.time,
                        ),
                    )
                    add(
                        wearableInput(
                            VitalMetricType.BLOOD_PRESSURE_DIASTOLIC,
                            record.diastolic.inMillimetersOfMercury,
                            "mmHg",
                            record.time,
                        ),
                    )
                }
                readAll<BloodGlucoseRecord>(healthClient, startTime, endTime).forEach { record ->
                    add(
                        wearableInput(
                            VitalMetricType.BLOOD_GLUCOSE,
                            record.level.inMilligramsPerDeciliter,
                            "mg/dL",
                            record.time,
                        ),
                    )
                }
            }.distinctBy { input -> input.metricType to input.recordedAt }
                .sortedBy { input -> input.recordedAt }

            when (val result = if (inputs.isEmpty()) {
                Result.Success(Unit)
            } else {
                vitalRepository.addVitals(inputs)
            }) {
                is Result.Success -> Result.Success(inputs.size)
                is Result.Error -> result
            }
        } catch (error: Exception) {
            Result.Error(error.toUserMessage())
        }
    }

    private fun client(): HealthConnectClient =
        HealthConnectClient.getOrCreate(context, PROVIDER_PACKAGE_NAME)

    private suspend inline fun <reified T : Record> readAll(
        healthClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant,
    ): List<T> {
        val records = mutableListOf<T>()
        var pageToken: String? = null
        do {
            val response = healthClient.readRecords(
                ReadRecordsRequest(
                    recordType = T::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    pageSize = PAGE_SIZE,
                    pageToken = pageToken,
                ),
            )
            records += response.records
            pageToken = response.pageToken
        } while (pageToken != null)
        return records
    }

    private fun wearableInput(
        metricType: VitalMetricType,
        value: Double,
        unit: String,
        recordedAt: Instant,
    ) = NewVitalInput(
        metricType = metricType,
        value = value,
        unit = unit,
        recordedAt = recordedAt,
        source = VitalSource.HEALTH_CONNECT,
        notes = "Imported from Health Connect.",
    )

    companion object {
        const val PROVIDER_PACKAGE_NAME = "com.google.android.apps.healthdata"
        private const val PAGE_SIZE = 1000
    }
}
