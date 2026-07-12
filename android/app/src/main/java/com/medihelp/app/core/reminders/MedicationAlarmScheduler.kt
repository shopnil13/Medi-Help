package com.medihelp.app.core.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.medihelp.app.feature_medications.data.local.entity.MedicationScheduleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    fun scheduleAllForMedication(
        medicationId: String,
        medicationName: String,
        schedules: List<MedicationScheduleEntity>,
    ) {
        schedules.forEach { schedule ->
            scheduleDailyReminder(
                scheduleId = schedule.id,
                medicationId = medicationId,
                medicationName = medicationName,
                doseAmount = schedule.doseAmount,
                timeOfDayMinutes = schedule.timeOfDayMinutes,
            )
        }
    }

    fun scheduleDailyReminder(
        scheduleId: String,
        medicationId: String,
        medicationName: String,
        doseAmount: String?,
        timeOfDayMinutes: Int,
    ) {
        scheduleAt(nextTriggerEpochMillis(timeOfDayMinutes), scheduleId, medicationId, medicationName, doseAmount, timeOfDayMinutes)
    }

    fun scheduleFromNowInMinutes(
        minutesFromNow: Long,
        scheduleId: String,
        medicationId: String,
        medicationName: String,
        doseAmount: String?,
        timeOfDayMinutes: Int,
    ) {
        val triggerAt = System.currentTimeMillis() + minutesFromNow * 60_000L
        scheduleAt(triggerAt, scheduleId, medicationId, medicationName, doseAmount, timeOfDayMinutes)
    }

    fun cancelReminder(scheduleId: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun cancelAll(scheduleIds: List<String>) {
        scheduleIds.forEach(::cancelReminder)
    }

    private fun scheduleAt(
        triggerAtEpochMillis: Long,
        scheduleId: String,
        medicationId: String,
        medicationName: String,
        doseAmount: String?,
        timeOfDayMinutes: Int,
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderIntentExtras.SCHEDULE_ID, scheduleId)
            putExtra(ReminderIntentExtras.MEDICATION_ID, medicationId)
            putExtra(ReminderIntentExtras.MEDICATION_NAME, medicationName)
            putExtra(ReminderIntentExtras.DOSE_AMOUNT, doseAmount)
            putExtra(ReminderIntentExtras.TIME_OF_DAY_MINUTES, timeOfDayMinutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtEpochMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtEpochMillis,
                pendingIntent,
            )
        }
    }

    private fun nextTriggerEpochMillis(timeOfDayMinutes: Int): Long {
        val zone = ZoneId.systemDefault()
        val time = LocalTime.ofSecondOfDay(timeOfDayMinutes * 60L)
        val now = java.time.ZonedDateTime.now(zone)
        var candidate = LocalDate.now(zone).atTime(time).atZone(zone)
        if (!candidate.isAfter(now)) {
            candidate = candidate.plusDays(1)
        }
        return candidate.toInstant().toEpochMilli()
    }
}
