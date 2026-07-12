package com.medihelp.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val MEDICATION_REMINDERS = "medication_reminders"

    fun registerAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            MEDICATION_REMINDERS,
            "Medicine reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Reminders to take your medicine on time"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
