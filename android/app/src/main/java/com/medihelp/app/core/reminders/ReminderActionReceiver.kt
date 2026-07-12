package com.medihelp.app.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medihelp.app.core.notifications.MedicationNotificationManager
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val SNOOZE_MINUTES = 10L

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: MedicationNotificationManager

    @Inject
    lateinit var alarmScheduler: MedicationAlarmScheduler

    @Inject
    lateinit var medicationRepository: MedicationRepository

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(ReminderIntentExtras.NOTIFICATION_ID, -1)
        val scheduleId = intent.getStringExtra(ReminderIntentExtras.SCHEDULE_ID) ?: return
        val medicationId = intent.getStringExtra(ReminderIntentExtras.MEDICATION_ID) ?: return
        val medicationName = intent.getStringExtra(ReminderIntentExtras.MEDICATION_NAME) ?: return
        val scheduledAtEpochMillis =
            intent.getLongExtra(ReminderIntentExtras.SCHEDULED_AT_EPOCH_MILLIS, System.currentTimeMillis())
        val timeOfDayMinutes = intent.getIntExtra(ReminderIntentExtras.TIME_OF_DAY_MINUTES, 0)
        val action = intent.getStringExtra(ReminderIntentExtras.ACTION) ?: return

        if (notificationId != -1) {
            notificationManager.cancel(notificationId)
        }

        if (action == "snoozed") {
            alarmScheduler.scheduleFromNowInMinutes(
                minutesFromNow = SNOOZE_MINUTES,
                scheduleId = scheduleId,
                medicationId = medicationId,
                medicationName = medicationName,
                doseAmount = null,
                timeOfDayMinutes = timeOfDayMinutes,
            )
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                medicationRepository.logReminderAction(
                    medicationId = medicationId,
                    scheduledAt = Instant.ofEpochMilli(scheduledAtEpochMillis),
                    action = action,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
