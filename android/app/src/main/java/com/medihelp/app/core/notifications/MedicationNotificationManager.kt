package com.medihelp.app.core.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.medihelp.app.MainActivity
import com.medihelp.app.R
import com.medihelp.app.core.reminders.ReminderActionReceiver
import com.medihelp.app.core.reminders.ReminderIntentExtras
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun showReminder(
        notificationId: Int,
        scheduleId: String,
        medicationId: String,
        medicationName: String,
        doseAmount: String?,
        scheduledAtEpochMillis: Long,
        timeOfDayMinutes: Int,
    ) {
        val contentText = if (doseAmount.isNullOrBlank()) {
            "It's time to take your medicine."
        } else {
            "Take $doseAmount now."
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.MEDICATION_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(medicationName)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(actionFor(notificationId, scheduleId, medicationId, medicationName, scheduledAtEpochMillis, timeOfDayMinutes, "taken", "Taken"))
            .addAction(actionFor(notificationId, scheduleId, medicationId, medicationName, scheduledAtEpochMillis, timeOfDayMinutes, "skipped", "Skip"))
            .addAction(actionFor(notificationId, scheduleId, medicationId, medicationName, scheduledAtEpochMillis, timeOfDayMinutes, "snoozed", "Snooze"))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun cancel(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun actionFor(
        notificationId: Int,
        scheduleId: String,
        medicationId: String,
        medicationName: String,
        scheduledAtEpochMillis: Long,
        timeOfDayMinutes: Int,
        action: String,
        label: String,
    ): NotificationCompat.Action {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            putExtra(ReminderIntentExtras.NOTIFICATION_ID, notificationId)
            putExtra(ReminderIntentExtras.SCHEDULE_ID, scheduleId)
            putExtra(ReminderIntentExtras.MEDICATION_ID, medicationId)
            putExtra(ReminderIntentExtras.MEDICATION_NAME, medicationName)
            putExtra(ReminderIntentExtras.SCHEDULED_AT_EPOCH_MILLIS, scheduledAtEpochMillis)
            putExtra(ReminderIntentExtras.TIME_OF_DAY_MINUTES, timeOfDayMinutes)
            putExtra(ReminderIntentExtras.ACTION, action)
        }
        val requestCode = "$scheduleId-$action".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Action.Builder(0, label, pendingIntent).build()
    }
}
