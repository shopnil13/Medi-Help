package com.medihelp.app.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medihelp.app.core.notifications.MedicationNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: MedicationNotificationManager

    @Inject
    lateinit var alarmScheduler: MedicationAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getStringExtra(ReminderIntentExtras.SCHEDULE_ID) ?: return
        val medicationId = intent.getStringExtra(ReminderIntentExtras.MEDICATION_ID) ?: return
        val medicationName = intent.getStringExtra(ReminderIntentExtras.MEDICATION_NAME) ?: return
        val doseAmount = intent.getStringExtra(ReminderIntentExtras.DOSE_AMOUNT)
        val timeOfDayMinutes = intent.getIntExtra(ReminderIntentExtras.TIME_OF_DAY_MINUTES, 0)

        notificationManager.showReminder(
            notificationId = scheduleId.hashCode(),
            scheduleId = scheduleId,
            medicationId = medicationId,
            medicationName = medicationName,
            doseAmount = doseAmount,
            scheduledAtEpochMillis = System.currentTimeMillis(),
            timeOfDayMinutes = timeOfDayMinutes,
        )

        alarmScheduler.scheduleDailyReminder(
            scheduleId = scheduleId,
            medicationId = medicationId,
            medicationName = medicationName,
            doseAmount = doseAmount,
            timeOfDayMinutes = timeOfDayMinutes,
        )
    }
}
